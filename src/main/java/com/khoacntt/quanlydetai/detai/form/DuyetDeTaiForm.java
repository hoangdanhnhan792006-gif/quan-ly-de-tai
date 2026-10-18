package com.khoacntt.quanlydetai.detai.form;

import jakarta.validation.constraints.*;

public class DuyetDeTaiForm {
    @NotNull(message = "Chọn quyết định duyệt")
    private Boolean chapThuan;
    @Size(max = 10000)
    private String nhanXet;

    public Boolean getChapThuan() {
        return chapThuan;
    }

    public String getNhanXet() {
        return nhanXet;
    }

    public void setChapThuan(Boolean chapThuan) {
        this.chapThuan = chapThuan;
    }

    public void setNhanXet(String nhanXet) {
        this.nhanXet = nhanXet;
    }
}
