package com.khoacntt.quanlydetai.dotdangky.form;

import com.khoacntt.quanlydetai.dotdangky.model.LoaiDeTai;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.*;

public class DotDangKyForm {
    @NotBlank(message = "Nhập mã đợt")
    @Pattern(regexp = "[A-Za-z0-9_-]{1,30}", message = "Mã đợt tối đa 30 chữ, số, gạch ngang hoặc gạch dưới")
    private String maDot;

    @NotBlank(message = "Nhập tên đợt") @Size(max = 200)
    private String tenDot;

    @NotNull(message = "Chọn loại đề tài")
    private LoaiDeTai loai;

    @NotNull(message = "Nhập đầy đủ các mốc thời gian bắt buộc")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime gvBatDau;

    @NotNull(message = "Nhập đầy đủ các mốc thời gian bắt buộc")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime gvKetThuc;

    @NotNull(message = "Nhập đầy đủ các mốc thời gian bắt buộc")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime svBatDau;

    @NotNull(message = "Nhập đầy đủ các mốc thời gian bắt buộc")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime svKetThuc;

    @NotNull(message = "Nhập đầy đủ các mốc thời gian bắt buộc")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime hanNopBaoCao;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime hanNopDiemPb;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngayBaoCaoHd;

    public String getMaDot() {
        return maDot;
    }

    public String getTenDot() {
        return tenDot;
    }

    public LoaiDeTai getLoai() {
        return loai;
    }

    public LocalDateTime getGvBatDau() {
        return gvBatDau;
    }

    public LocalDateTime getGvKetThuc() {
        return gvKetThuc;
    }

    public LocalDateTime getSvBatDau() {
        return svBatDau;
    }

    public LocalDateTime getSvKetThuc() {
        return svKetThuc;
    }

    public LocalDateTime getHanNopBaoCao() {
        return hanNopBaoCao;
    }

    public LocalDateTime getHanNopDiemPb() {
        return hanNopDiemPb;
    }

    public LocalDate getNgayBaoCaoHd() {
        return ngayBaoCaoHd;
    }

    public void setMaDot(String maDot) {
        this.maDot = maDot;
    }

    public void setTenDot(String tenDot) {
        this.tenDot = tenDot;
    }

    public void setLoai(LoaiDeTai loai) {
        this.loai = loai;
    }

    public void setGvBatDau(LocalDateTime gvBatDau) {
        this.gvBatDau = gvBatDau;
    }

    public void setGvKetThuc(LocalDateTime gvKetThuc) {
        this.gvKetThuc = gvKetThuc;
    }

    public void setSvBatDau(LocalDateTime svBatDau) {
        this.svBatDau = svBatDau;
    }

    public void setSvKetThuc(LocalDateTime svKetThuc) {
        this.svKetThuc = svKetThuc;
    }

    public void setHanNopBaoCao(LocalDateTime hanNopBaoCao) {
        this.hanNopBaoCao = hanNopBaoCao;
    }

    public void setHanNopDiemPb(LocalDateTime hanNopDiemPb) {
        this.hanNopDiemPb = hanNopDiemPb;
    }

    public void setNgayBaoCaoHd(LocalDate ngayBaoCaoHd) {
        this.ngayBaoCaoHd = ngayBaoCaoHd;
    }
}
