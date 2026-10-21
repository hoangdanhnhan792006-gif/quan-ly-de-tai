package com.khoacntt.quanlydetai.thongbao.service;
import com.khoacntt.quanlydetai.account.model.*;
import com.khoacntt.quanlydetai.common.exception.*;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.common.storage.*;
import com.khoacntt.quanlydetai.common.time.ThoiGian;
import com.khoacntt.quanlydetai.thongbao.dto.ThongBaoDto;
import com.khoacntt.quanlydetai.thongbao.form.ThongBaoForm;
import com.khoacntt.quanlydetai.thongbao.model.*;
import com.khoacntt.quanlydetai.thongbao.repository.*;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.time.Clock;
import java.util.*;
@Service
@Validated
@Transactional(readOnly = true)
@PreAuthorize("isAuthenticated()")
public class ThongBaoService {
    private final ThongBaoRepository thongBaos;
    private final ThongBaoTepRepository teps;
    private final NguoiDungHienTai actor;
    private final Clock clock;
    public ThongBaoService(ThongBaoRepository thongBaos, ThongBaoTepRepository teps, NguoiDungHienTai actor,
            Clock clock) {
        this.thongBaos = thongBaos;
        this.teps = teps;
        this.actor = actor;
        this.clock = clock;
    }
    public List<ThongBaoDto> congKhai() {
        return danhSachDto(thongBaos.findByTrangThaiOrderByCongBoLucDesc(TrangThaiThongBao.DA_CONG_BO));
    }
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public List<ThongBaoDto> quanLy() {
        var tk = actor.batBuoc();
        return danhSachDto(thongBaos.quanLy(tk.coVaiTro(VaiTro.ADMIN), tk.getId()));
    }
    public ThongBaoDto chiTiet(Integer id) {
        var t = tim(id);
        kiemTraXem(t);
        return dto(t);
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public Integer tao(@Valid ThongBaoForm f) {
        ThongBao t = new ThongBao(actor.batBuoc());
        t.sua(f.getTieuDe().trim(), f.getNoiDung().trim(), f.getDonViBanHanh());
        return thongBaos.saveAndFlush(t).getId();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public void sua(Integer id, @Valid ThongBaoForm f) {
        var t = khoa(id);
        kiemTraSua(t);
        t.sua(f.getTieuDe().trim(), f.getNoiDung().trim(), f.getDonViBanHanh());
    }
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public void kiemTraThemTep(Integer id) {
        kiemTraSua(tim(id));
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public void themTep(Integer id, TepDaLuu file) {
        var t = khoa(id);
        kiemTraSua(t);
        if (teps.findByThongBaoIdOrderByIdAsc(id).size() >= 10)
            throw new NghiepVuException("Một thông báo tối đa 10 tệp đính kèm");
        teps.saveAndFlush(new ThongBaoTep(t, file, actor.batBuoc()));
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public void congBo(Integer id) {
        var t = khoa(id);
        kiemTraSua(t);
        t.congBo(ThoiGian.hienTai(clock));
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public void thuHoi(Integer id) {
        var t = khoa(id);
        if (!coQuyen(t))
            throw new AccessDeniedException("Bạn không quản lý thông báo này");
        if (t.getTrangThai() != TrangThaiThongBao.DA_CONG_BO)
            throw new NghiepVuException("Thông báo chưa công bố");
        t.thuHoi();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
    public void xoa(Integer id) {
        var t = khoa(id);
        kiemTraSua(t);
        if (t.getTrangThai() != TrangThaiThongBao.NHAP)
            throw new NghiepVuException("Chỉ xóa thông báo nháp");
        if (!teps.findByThongBaoIdOrderByIdAsc(id).isEmpty())
            throw new NghiepVuException("Thông báo đã có tệp đính kèm; giữ lại bản nháp hoặc sử dụng thu hồi sau công bố");
        thongBaos.delete(t);
        thongBaos.flush();
    }

    public TaiTepDto taiTep(Integer id) {
        var t = teps.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy tệp"));
        kiemTraXem(t.getThongBao());
        return new TaiTepDto(t.getKhoaLuuTru(), t.getTenTepGoc(), t.getLoaiMime(), t.getKichThuocByte());
    }
    public ThongBaoForm formSua(Integer id) {
        var t = tim(id);
        kiemTraSua(t);
        var f = new ThongBaoForm();
        f.setTieuDe(t.getTieuDe());
        f.setNoiDung(t.getNoiDung());
        f.setDonViBanHanh(t.getDonViBanHanh());
        return f;
    }
    private boolean coQuyen(ThongBao t) {
        var tk = actor.batBuoc();
        return tk.coVaiTro(VaiTro.ADMIN) || ((tk.coVaiTro(VaiTro.CAN_BO_KHOA) || tk.coVaiTro(VaiTro.TRUONG_KHOA))
                && tk.getId().equals(t.getNguoiDang().getId()));
    }
    private void kiemTraSua(ThongBao t) {
        if (!coQuyen(t))
            throw new AccessDeniedException("Chỉ người đăng hoặc admin được sửa thông báo");
        if (t.getTrangThai() == TrangThaiThongBao.DA_CONG_BO)
            throw new NghiepVuException("Thu hồi thông báo trước khi sửa");
    }
    private void kiemTraXem(ThongBao t) {
        if (t.getTrangThai() != TrangThaiThongBao.DA_CONG_BO && !coQuyen(t))
            throw new AccessDeniedException("Thông báo chưa công bố hoặc đã thu hồi");
    }
    private ThongBao tim(Integer id) {
        return thongBaos.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy thông báo"));
    }
    private ThongBao khoa(Integer id) {
        return thongBaos.timVaKhoa(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy thông báo"));
    }
    private List<ThongBaoDto> danhSachDto(List<ThongBao> ds) {
        if (ds.isEmpty()) return List.of();
        var tk = actor.batBuoc();
        var tepTheoThongBao = teps.findByThongBaoIdInOrderByIdAsc(ds.stream().map(ThongBao::getId).toList()).stream()
                .collect(java.util.stream.Collectors.groupingBy(t -> t.getThongBao().getId()));
        return ds.stream().map(t -> dto(t, tk.coVaiTro(VaiTro.ADMIN)
                || ((tk.coVaiTro(VaiTro.TRUONG_KHOA) || tk.coVaiTro(VaiTro.CAN_BO_KHOA))
                    && tk.getId().equals(t.getNguoiDang().getId())),
                tepTheoThongBao.getOrDefault(t.getId(), List.of()))).toList();
    }
    private ThongBaoDto dto(ThongBao t) {
        return dto(t, coQuyen(t), teps.findByThongBaoIdOrderByIdAsc(t.getId()));
    }
    private ThongBaoDto dto(ThongBao t, boolean coQuyen, List<ThongBaoTep> files) {
        return new ThongBaoDto(t.getId(), t.getTieuDe(), t.getNoiDung(), t.getDonViBanHanh(),
                t.getNguoiDang().getHoTen(), t.getTrangThai(), ThoiGian.sangGioVietNam(t.getCongBoLuc()), coQuyen,
                files.stream()
                        .map(f -> new ThongBaoDto.TepDto(f.getId(), f.getTenTepGoc(), f.getKichThuocByte())).toList());
    }
}
