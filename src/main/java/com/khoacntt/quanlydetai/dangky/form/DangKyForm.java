package com.khoacntt.quanlydetai.dangky.form;

import jakarta.validation.constraints.*;

public class DangKyForm {
    @NotNull @Positive
    private Integer deTaiId;
    public Integer getDeTaiId() { return deTaiId; }
    public void setDeTaiId(Integer deTaiId) { this.deTaiId = deTaiId; }
}
