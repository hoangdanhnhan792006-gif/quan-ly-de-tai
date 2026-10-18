package com.khoacntt.quanlydetai.danhgia.form;
import jakarta.validation.constraints.*;
import java.util.Set;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import com.khoacntt.quanlydetai.thongbao.model.DonViBanHanh;
public class TongHopForm {
    @NotBlank
    @Size(max = 10000)
    private String nhanXet;
    public String getNhanXet() {
        return nhanXet;
    }
    public void setNhanXet(String value) {
        this.nhanXet = value;
    }
}
