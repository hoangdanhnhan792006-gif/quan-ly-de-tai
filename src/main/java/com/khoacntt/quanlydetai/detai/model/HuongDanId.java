package com.khoacntt.quanlydetai.detai.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class HuongDanId implements Serializable {
    @Column(name = "de_tai_id")
    private Integer deTaiId;
    @Column(name = "giang_vien_id")
    private Integer giangVienId;

    protected HuongDanId() {}

    public HuongDanId(Integer deTaiId, Integer giangVienId) {
        this.deTaiId = deTaiId;
        this.giangVienId = giangVienId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof HuongDanId id)) return false;
        return Objects.equals(deTaiId, id.deTaiId) && Objects.equals(giangVienId, id.giangVienId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deTaiId, giangVienId);
    }
}
