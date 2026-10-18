package com.khoacntt.quanlydetai.nhom.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ThanhVienNhomId implements Serializable {
    @Column(name = "nhom_id") private Integer nhomId;
    @Column(name = "sinh_vien_id") private Integer sinhVienId;
    protected ThanhVienNhomId() {}
    public ThanhVienNhomId(Integer nhomId, Integer sinhVienId) {
        this.nhomId = nhomId; this.sinhVienId = sinhVienId;
    }
    @Override public boolean equals(Object o) {
        return o instanceof ThanhVienNhomId other && Objects.equals(nhomId, other.nhomId)
                && Objects.equals(sinhVienId, other.sinhVienId);
    }
    @Override public int hashCode() { return Objects.hash(nhomId, sinhVienId); }
}
