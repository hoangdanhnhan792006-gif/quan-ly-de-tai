package com.khoacntt.quanlydetai.detai.form;

import jakarta.validation.constraints.*;
import java.util.*;

public class HuongDanForm {
    @NotEmpty(message = "Chọn ít nhất một giảng viên hướng dẫn")
    @Size(max = 2, message = "Tối đa hai giảng viên hướng dẫn")
    private Set<@NotNull @Positive Integer> giangVienIds = new HashSet<>();

    public Set<Integer> getGiangVienIds() {
        return giangVienIds;
    }

    public void setGiangVienIds(Set<Integer> giangVienIds) {
        this.giangVienIds = giangVienIds;
    }
}
