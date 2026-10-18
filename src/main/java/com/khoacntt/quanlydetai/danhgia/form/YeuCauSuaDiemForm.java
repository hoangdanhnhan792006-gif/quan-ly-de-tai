package com.khoacntt.quanlydetai.danhgia.form;
import jakarta.validation.constraints.*;
import java.util.Set;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import com.khoacntt.quanlydetai.thongbao.model.DonViBanHanh;
public class YeuCauSuaDiemForm {
    @NotBlank
    @Size(max = 5000)
    private String lyDo;
    public String getLyDo() {
        return lyDo;
    }
    public void setLyDo(String value) {
        this.lyDo = value;
    }
}
