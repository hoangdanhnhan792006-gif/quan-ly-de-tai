package com.khoacntt.quanlydetai.danhgia.service;
import com.khoacntt.quanlydetai.account.model.*;
import com.khoacntt.quanlydetai.common.exception.*;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.danhgia.model.*;
import com.khoacntt.quanlydetai.danhgia.repository.*;
import com.khoacntt.quanlydetai.detai.repository.HuongDanRepository;
import com.khoacntt.quanlydetai.dotdangky.model.*;
import com.khoacntt.quanlydetai.hoso.model.GiangVien;
import com.khoacntt.quanlydetai.hoso.repository.GiangVienRepository;
import com.khoacntt.quanlydetai.nhom.service.QuyTacNhom;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import java.util.*;
@Component
public class QuyTacDanhGia {
    public static final List<TrangThaiYeuCauSuaDiem> DANG_XU_LY = List.of(TrangThaiYeuCauSuaDiem.CHO_DUYET,
            TrangThaiYeuCauSuaDiem.CHAP_THUAN, TrangThaiYeuCauSuaDiem.DA_NOP_DIEU_CHINH);
    private final DanhGiaRepository danhGias;
    private final PhanCongChamRepository phanCongs;
    private final ThanhVienHoiDongRepository thanhViens;
    private final GiangVienRepository giangViens;
    private final HuongDanRepository huongDans;
    private final QuyTacNhom nhom;
    private final NguoiDungHienTai actor;
    public QuyTacDanhGia(DanhGiaRepository danhGias, PhanCongChamRepository phanCongs,
            ThanhVienHoiDongRepository thanhViens, GiangVienRepository giangViens, HuongDanRepository huongDans,
            QuyTacNhom nhom, NguoiDungHienTai actor) {
        this.danhGias = danhGias;
        this.phanCongs = phanCongs;
        this.thanhViens = thanhViens;
        this.giangViens = giangViens;
        this.huongDans = huongDans;
        this.nhom = nhom;
        this.actor = actor;
    }
    public TaiKhoan actor() {
        return actor.batBuoc();
    }
    public boolean laKhoa() {
        var t = actor();
        return t.coVaiTro(VaiTro.TRUONG_KHOA) || t.coVaiTro(VaiTro.CAN_BO_KHOA);
    }
    public boolean laChuTich(DanhGia d) {
        return d.getHoiDong() != null && actor().coVaiTro(VaiTro.GIANG_VIEN)
                && thanhViens.existsByHoiDongIdAndGiangVienTaiKhoanIdAndVaiTro(d.getHoiDong().getId(), actor().getId(),
                        VaiTroHoiDong.CHU_TICH);
    }
    public boolean duocXem(DanhGia d) {
        return laKhoa() || (actor().coVaiTro(VaiTro.GIANG_VIEN)
                && phanCongs.existsByDanhGiaDangKyIdAndGiangVienTaiKhoanId(d.getDangKy().getId(), actor().getId()));
    }
    public void kiemTraXem(DanhGia d) {
        if (!duocXem(d))
            throw new AccessDeniedException("Bạn không được xem lần đánh giá này");
    }
    public DanhGia tim(Integer id) {
        return danhGias.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy lần đánh giá"));
    }
    public DanhGia khoa(Integer id) {
        Integer nhomId = danhGias.timNhomId(id)
                .orElseThrow(() -> new KhongTimThayException("Không tìm thấy lần đánh giá"));
        nhom.khoaNhom(nhomId, null);
        return tim(id);
    }
    public PhanCongCham khoaPhanCong(Integer id) {
        Integer dgId = phanCongs.timDanhGiaId(id)
                .orElseThrow(() -> new KhongTimThayException("Không tìm thấy phân công"));
        khoa(dgId);
        return phanCongs.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy phân công"));
    }
    public void chuPhanCong(PhanCongCham pc) {
        if (!actor().coVaiTro(VaiTro.GIANG_VIEN) || !pc.getGiangVien().getTaiKhoan().getId().equals(actor().getId()))
            throw new AccessDeniedException("Chỉ giảng viên được phân công mới được nhập điểm");
        kiemTraGv(pc.getGiangVien());
        khongLaHuongDan(pc.getDanhGia().getDangKy().getDeTai().getId(), pc.getGiangVien());
    }
    public GiangVien giangVien(Integer id) {
        GiangVien gv = giangViens.findById(id)
                .orElseThrow(() -> new KhongTimThayException("Không tìm thấy giảng viên"));
        kiemTraGv(gv);
        return gv;
    }
    public void kiemTraGv(GiangVien gv) {
        if (gv.getTaiKhoan().getTrangThai() != TrangThaiTaiKhoan.HOAT_DONG
                || !gv.getTaiKhoan().coVaiTro(VaiTro.GIANG_VIEN))
            throw new NghiepVuException("Giảng viên phải có tài khoản hoạt động và vai trò GIANG_VIEN");
    }
    public void khongLaHuongDan(Integer topic, GiangVien gv) {
        if (huongDans.existsByDeTaiIdAndGiangVienTaiKhoanId(topic, gv.getTaiKhoan().getId()))
            throw new NghiepVuException("GVHD không được phản biện hoặc chấm đề tài mình hướng dẫn");
    }
    public boolean canHoiDong(LoaiDeTai loai) {
        return loai == LoaiDeTai.NCKH || loai == LoaiDeTai.KLTN;
    }
    public void dangTrienKhai(DotDangKy dot) {
        if (dot.getTrangThai() != TrangThaiDotDangKy.TRIEN_KHAI)
            throw new NghiepVuException("Đợt không còn triển khai");
    }
    public void duocDieuChinh(DanhGia dg) {
        var state = dg.getDangKy().getNhom().getDot().getTrangThai();
        if (state == TrangThaiDotDangKy.HUY || state == TrangThaiDotDangKy.NHAP)
            throw new NghiepVuException("Đợt không cho phép xử lý điểm");
    }

    public Set<Integer> hoiDongChuTich(Collection<Integer> ids, TaiKhoan tk) {
        if (ids.isEmpty() || !tk.coVaiTro(VaiTro.GIANG_VIEN)) return Set.of();
        return thanhViens.hoiDongChuTich(ids, tk.getId());
    }

}
