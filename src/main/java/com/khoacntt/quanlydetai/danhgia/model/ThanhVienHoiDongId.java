package com.khoacntt.quanlydetai.danhgia.model;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
@Embeddable
public class ThanhVienHoiDongId implements Serializable {
    @Column(name = "hoi_dong_id")
    private Integer hoiDongId;
    @Column(name = "giang_vien_id")
    private Integer giangVienId;
    protected ThanhVienHoiDongId() {
    }
    public ThanhVienHoiDongId(Integer owner, Integer target) {
        this.hoiDongId = owner;
        this.giangVienId = target;
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof ThanhVienHoiDongId x && Objects.equals(hoiDongId, x.hoiDongId)
                && Objects.equals(giangVienId, x.giangVienId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(hoiDongId, giangVienId);
    }
}
