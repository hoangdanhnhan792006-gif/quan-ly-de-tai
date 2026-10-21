package com.khoacntt.quanlydetai.danhgia.service;
import com.khoacntt.quanlydetai.common.exception.*;
import com.khoacntt.quanlydetai.danhgia.dto.HoiDongDto;
import com.khoacntt.quanlydetai.danhgia.form.HoiDongForm;
import com.khoacntt.quanlydetai.danhgia.model.*;
import com.khoacntt.quanlydetai.danhgia.repository.*;
import com.khoacntt.quanlydetai.nhom.service.QuyTacNhom;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.util.*;
@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
public class HoiDongService {
    private final HoiDongRepository hoiDongs;
    private final ThanhVienHoiDongRepository thanhViens;
    private final DanhGiaRepository danhGias;
    private final QuyTacDanhGia rules;
    private final QuyTacNhom nhom;
    public HoiDongService(HoiDongRepository hoiDongs, ThanhVienHoiDongRepository thanhViens, DanhGiaRepository danhGias,
            QuyTacDanhGia rules, QuyTacNhom nhom) {
        this.hoiDongs = hoiDongs;
        this.thanhViens = thanhViens;
        this.danhGias = danhGias;
        this.rules = rules;
        this.nhom = nhom;
    }
    public List<HoiDongDto> danhSach() {
        var ds = hoiDongs.findAllByOrderByIdDesc();
        if (ds.isEmpty()) return List.of();
        var thanhVienTheoHoiDong = thanhViens.findByHoiDongIdInOrderByGiangVienMaGvAsc(ds.stream().map(HoiDong::getId).toList()).stream()
                .collect(java.util.stream.Collectors.groupingBy(t -> t.getHoiDong().getId()));
        return ds.stream().map(h -> dto(h, thanhVienTheoHoiDong.getOrDefault(h.getId(), List.of()))).toList();
    }
    public HoiDongDto chiTiet(Integer id) {
        return dto(hoiDongs.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy hội đồng")));
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Integer tao(@Valid HoiDongForm f) {
        var dot = nhom.khoaDot(f.getDotId());
        rules.dangTrienKhai(dot);
        if (!rules.canHoiDong(dot.getLoai()))
            throw new NghiepVuException("Chỉ NCKH và KLTN có hội đồng");
        String ma = f.getMaHoiDong().trim().toUpperCase(Locale.ROOT);
        if (hoiDongs.existsByDotIdAndMaHoiDong(dot.getId(), ma))
            throw new NghiepVuException("Mã hội đồng đã tồn tại trong đợt");
        kiemTraThanhPhan(f);
        HoiDong hd = hoiDongs.saveAndFlush(new HoiDong(dot, ma, f.getTenHoiDong().trim(), rules.actor()));
        luuThanhVien(hd, f);
        return hd.getId();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void sua(Integer id, @Valid HoiDongForm f) {
        HoiDong h = khoa(id);
        if (h.getTrangThai() != TrangThaiHoiDong.NHAP)
            throw new NghiepVuException("Chỉ sửa thành phần hội đồng còn nháp");
        if (!h.getDot().getId().equals(f.getDotId())
                || !h.getMaHoiDong().equals(f.getMaHoiDong().trim().toUpperCase(Locale.ROOT)))
            throw new NghiepVuException("Không thay đổi đợt hoặc mã hội đồng");
        kiemTraThanhPhan(f);
        h.doiTen(f.getTenHoiDong().trim());
        thanhViens.deleteAll(thanhViens.findByHoiDongIdOrderByGiangVienMaGvAsc(id));
        thanhViens.flush();
        luuThanhVien(h, f);
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void chot(Integer id) {
        HoiDong h = khoa(id);
        if (h.getTrangThai() != TrangThaiHoiDong.NHAP)
            throw new NghiepVuException("Hội đồng không còn nháp");
        var ds = thanhViens.findByHoiDongIdOrderByGiangVienMaGvAsc(id);
        if (ds.size() < 3 || ds.size() > 5
                || ds.stream().filter(t -> t.getVaiTro() == VaiTroHoiDong.CHU_TICH).count() != 1
                || ds.stream().filter(t -> t.getVaiTro() == VaiTroHoiDong.THU_KY).count() != 1)
            throw new NghiepVuException("Cần 3–5 GV, đúng một chủ tịch và một thư ký");
        ds.forEach(t -> rules.kiemTraGv(t.getGiangVien()));
        h.chot();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void hoanThanh(Integer id) {
        HoiDong h = khoa(id);
        var ds = danhGias.findByHoiDongId(id);
        if (h.getTrangThai() != TrangThaiHoiDong.DA_CHOT || ds.isEmpty()
                || ds.stream().anyMatch(d -> d.getTrangThaiHoiDong() != TrangThaiDanhGiaHoiDong.HOAN_TAT))
            throw new NghiepVuException("Chỉ hoàn thành khi đã chốt và tất cả đề tài được giao đã chấm xong hội đồng");
        h.hoanThanh();
    }
    private HoiDong khoa(Integer id) {
        var dotId = hoiDongs.timDotId(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy hội đồng"));
        var dot = nhom.khoaDot(dotId);
        rules.dangTrienKhai(dot);
        return hoiDongs.timVaKhoa(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy hội đồng"));
    }
    private void kiemTraThanhPhan(HoiDongForm f) {
        Set<Integer> ids = new HashSet<>(f.getUyVienIds());
        if (!ids.add(f.getChuTichId()) || !ids.add(f.getThuKyId()) || ids.size() < 3 || ids.size() > 5)
            throw new NghiepVuException("Chọn 3–5 GV khác nhau; chủ tịch, thư ký và ủy viên không trùng nhau");
        ids.forEach(rules::giangVien);
    }
    private void luuThanhVien(HoiDong h, HoiDongForm f) {
        thanhViens.save(new ThanhVienHoiDong(h, rules.giangVien(f.getChuTichId()), VaiTroHoiDong.CHU_TICH));
        thanhViens.save(new ThanhVienHoiDong(h, rules.giangVien(f.getThuKyId()), VaiTroHoiDong.THU_KY));
        for (Integer id : f.getUyVienIds())
            thanhViens.save(new ThanhVienHoiDong(h, rules.giangVien(id), VaiTroHoiDong.UY_VIEN));
        thanhViens.flush();
    }
    private HoiDongDto dto(HoiDong h) {
        return dto(h, thanhViens.findByHoiDongIdOrderByGiangVienMaGvAsc(h.getId()));
    }
    private HoiDongDto dto(HoiDong h, List<ThanhVienHoiDong> ds) {
        return new HoiDongDto(h.getId(), h.getDot().getId(), h.getDot().getTenDot(), h.getMaHoiDong(),
                h.getTenHoiDong(), h.getTrangThai(), ds.stream().map(t -> new HoiDongDto.ThanhVienDto(t.getGiangVien().getId(),
                                t.getGiangVien().getMaGv(), t.getGiangVien().getTaiKhoan().getHoTen(), t.getVaiTro()))
                        .toList());
    }
}
