package com.khoacntt.quanlydetai.dangky.form;

import jakarta.validation.constraints.*;

public class DuyetDangKyForm {
    @NotNull @Positive
    private Integer deTaiId;
    public Integer getDeTaiId() { return deTaiId; }
    public void setDeTaiId(Integer deTaiId) { this.deTaiId = deTaiId; }
    @NotNull
    private Boolean chapThuan;
    public Boolean getChapThuan() { return chapThuan; }
    public void setChapThuan(Boolean chapThuan) { this.chapThuan = chapThuan; }
    @Size(max = 2000)
    private String lyDo;
    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
}
