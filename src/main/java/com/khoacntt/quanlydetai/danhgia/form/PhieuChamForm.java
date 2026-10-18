package com.khoacntt.quanlydetai.danhgia.form;
import jakarta.validation.constraints.*;
import java.util.Set;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import com.khoacntt.quanlydetai.thongbao.model.DonViBanHanh;
public class PhieuChamForm {
    @DecimalMin("0.00")
    @DecimalMax("10.00")
    @Digits(integer = 2, fraction = 2)
    private BigDecimal diem;
    public BigDecimal getDiem() {
        return diem;
    }
    public void setDiem(BigDecimal value) {
        this.diem = value;
    }
    @Size(max = 10000)
    private String nhanXet;
    public String getNhanXet() {
        return nhanXet;
    }
    public void setNhanXet(String value) {
        this.nhanXet = value;
    }
    @NotNull
    private Boolean nop;
    public Boolean getNop() {
        return nop;
    }
    public void setNop(Boolean value) {
        this.nop = value;
    }
}
