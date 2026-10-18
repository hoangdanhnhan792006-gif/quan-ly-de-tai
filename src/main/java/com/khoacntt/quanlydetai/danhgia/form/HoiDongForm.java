package com.khoacntt.quanlydetai.danhgia.form;
import jakarta.validation.constraints.*;
import java.util.Set;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import com.khoacntt.quanlydetai.thongbao.model.DonViBanHanh;
public class HoiDongForm {
    @NotNull
    @Positive
    private Integer dotId;
    public Integer getDotId() {
        return dotId;
    }
    public void setDotId(Integer value) {
        this.dotId = value;
    }
    @NotBlank
    @Pattern(regexp = "[A-Za-z0-9_-]{1,30}")
    private String maHoiDong;
    public String getMaHoiDong() {
        return maHoiDong;
    }
    public void setMaHoiDong(String value) {
        this.maHoiDong = value;
    }
    @NotBlank
    @Size(max = 200)
    private String tenHoiDong;
    public String getTenHoiDong() {
        return tenHoiDong;
    }
    public void setTenHoiDong(String value) {
        this.tenHoiDong = value;
    }
    @NotNull
    @Positive
    private Integer chuTichId;
    public Integer getChuTichId() {
        return chuTichId;
    }
    public void setChuTichId(Integer value) {
        this.chuTichId = value;
    }
    @NotNull
    @Positive
    private Integer thuKyId;
    public Integer getThuKyId() {
        return thuKyId;
    }
    public void setThuKyId(Integer value) {
        this.thuKyId = value;
    }
    @NotEmpty
    @Size(max = 3)
    private Set<@NotNull @Positive Integer> uyVienIds;
    public Set<@NotNull @Positive Integer> getUyVienIds() {
        return uyVienIds;
    }
    public void setUyVienIds(Set<@NotNull @Positive Integer> value) {
        this.uyVienIds = value;
    }
}
