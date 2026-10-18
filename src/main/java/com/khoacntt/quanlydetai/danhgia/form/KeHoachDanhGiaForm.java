package com.khoacntt.quanlydetai.danhgia.form;
import jakarta.validation.constraints.*;
import java.util.Set;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import com.khoacntt.quanlydetai.thongbao.model.DonViBanHanh;
public class KeHoachDanhGiaForm {
    @NotNull
    @Positive
    private Integer dangKyId;
    public Integer getDangKyId() {
        return dangKyId;
    }
    public void setDangKyId(Integer value) {
        this.dangKyId = value;
    }
    @NotNull
    @Positive
    private Integer phanBienId;
    public Integer getPhanBienId() {
        return phanBienId;
    }
    public void setPhanBienId(Integer value) {
        this.phanBienId = value;
    }
    @Positive
    private Integer hoiDongId;
    public Integer getHoiDongId() {
        return hoiDongId;
    }
    public void setHoiDongId(Integer value) {
        this.hoiDongId = value;
    }
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lichPhanBienLuc;
    public LocalDateTime getLichPhanBienLuc() {
        return lichPhanBienLuc;
    }
    public void setLichPhanBienLuc(LocalDateTime value) {
        this.lichPhanBienLuc = value;
    }
    @NotBlank
    @Size(max = 255)
    private String diaDiemPhanBien;
    public String getDiaDiemPhanBien() {
        return diaDiemPhanBien;
    }
    public void setDiaDiemPhanBien(String value) {
        this.diaDiemPhanBien = value;
    }
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime hoiDongBatDau;
    public LocalDateTime getHoiDongBatDau() {
        return hoiDongBatDau;
    }
    public void setHoiDongBatDau(LocalDateTime value) {
        this.hoiDongBatDau = value;
    }
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime hoiDongKetThuc;
    public LocalDateTime getHoiDongKetThuc() {
        return hoiDongKetThuc;
    }
    public void setHoiDongKetThuc(LocalDateTime value) {
        this.hoiDongKetThuc = value;
    }
    @Size(max = 255)
    private String diaDiemHoiDong;
    public String getDiaDiemHoiDong() {
        return diaDiemHoiDong;
    }
    public void setDiaDiemHoiDong(String value) {
        this.diaDiemHoiDong = value;
    }
}
