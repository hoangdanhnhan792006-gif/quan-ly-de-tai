package com.khoacntt.quanlydetai.baocao.form;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
public class NopBaoCaoForm {
    @Size(max = 5000)
    private String ghiChu;
    private MultipartFile pdf;
    private MultipartFile slide;
    private MultipartFile source;
    public String getGhiChu() {
        return ghiChu;
    }
    public void setGhiChu(String v) {
        ghiChu = v;
    }
    public MultipartFile getPdf() {
        return pdf;
    }
    public void setPdf(MultipartFile v) {
        pdf = v;
    }
    public MultipartFile getSlide() {
        return slide;
    }
    public void setSlide(MultipartFile v) {
        slide = v;
    }
    public MultipartFile getSource() {
        return source;
    }
    public void setSource(MultipartFile v) {
        source = v;
    }
}
