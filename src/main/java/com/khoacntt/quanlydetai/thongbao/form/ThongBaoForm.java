package com.khoacntt.quanlydetai.thongbao.form;
import jakarta.validation.constraints.*;
import java.util.Set;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import com.khoacntt.quanlydetai.thongbao.model.DonViBanHanh;
public class ThongBaoForm {
    @NotBlank
    @Size(max = 255)
    private String tieuDe;
    public String getTieuDe() {
        return tieuDe;
    }
    public void setTieuDe(String value) {
        this.tieuDe = value;
    }
    @NotBlank
    @Size(max = 15000)
    private String noiDung;
    public String getNoiDung() {
        return noiDung;
    }
    public void setNoiDung(String value) {
        this.noiDung = value;
    }
    @NotNull
    private DonViBanHanh donViBanHanh;
    public DonViBanHanh getDonViBanHanh() {
        return donViBanHanh;
    }
    public void setDonViBanHanh(DonViBanHanh value) {
        this.donViBanHanh = value;
    }
}
