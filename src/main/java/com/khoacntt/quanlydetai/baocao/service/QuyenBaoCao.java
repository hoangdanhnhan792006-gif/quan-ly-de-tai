package com.khoacntt.quanlydetai.baocao.service;
import com.khoacntt.quanlydetai.account.model.*;
import com.khoacntt.quanlydetai.common.security.NguoiDungHienTai;
import com.khoacntt.quanlydetai.dangky.model.DangKy;
import com.khoacntt.quanlydetai.detai.repository.HuongDanRepository;
import com.khoacntt.quanlydetai.danhgia.repository.PhanCongChamRepository;
import com.khoacntt.quanlydetai.hoso.repository.SinhVienRepository;
import com.khoacntt.quanlydetai.nhom.repository.ThanhVienNhomRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
@Component
public class QuyenBaoCao {
    private final NguoiDungHienTai actor;
    private final SinhVienRepository sinhViens;
    private final ThanhVienNhomRepository thanhViens;
    private final HuongDanRepository huongDans;
    private final PhanCongChamRepository phanCongs;
    public QuyenBaoCao(NguoiDungHienTai actor, SinhVienRepository sinhViens, ThanhVienNhomRepository thanhViens,
            HuongDanRepository huongDans, PhanCongChamRepository phanCongs) {
        this.actor = actor;
        this.sinhViens = sinhViens;
        this.thanhViens = thanhViens;
        this.huongDans = huongDans;
        this.phanCongs = phanCongs;
    }
    public boolean duocXem(DangKy dk) {
        TaiKhoan tk = actor.batBuoc();
        if (tk.coVaiTro(VaiTro.TRUONG_KHOA) || tk.coVaiTro(VaiTro.CAN_BO_KHOA))
            return true;
        if (tk.coVaiTro(VaiTro.SINH_VIEN) && sinhViens.findByTaiKhoanId(tk.getId())
                .flatMap(s -> thanhViens.findByNhomIdAndSinhVienId(dk.getNhom().getId(), s.getId())).isPresent())
            return true;
        return tk.coVaiTro(VaiTro.GIANG_VIEN)
                && (huongDans.existsByDeTaiIdAndGiangVienTaiKhoanId(dk.getDeTai().getId(), tk.getId())
                        || phanCongs.existsByDanhGiaDangKyIdAndGiangVienTaiKhoanId(dk.getId(), tk.getId()));
    }
    public void kiemTraXem(DangKy dk) {
        if (!duocXem(dk))
            throw new AccessDeniedException("Bạn không được xem báo cáo của nhóm này");
    }

    public TaiKhoan nguoiDung() {
        return actor.batBuoc();
    }

}
