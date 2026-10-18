package com.khoacntt.quanlydetai.danhgia.model;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
@Embeddable
public class ChiTietKetQuaId implements Serializable {
    @Column(name = "ket_qua_id")
    private Integer ketQuaId;
    @Column(name = "phan_cong_cham_id")
    private Integer phanCongChamId;
    protected ChiTietKetQuaId() {
    }
    public ChiTietKetQuaId(Integer owner, Integer target) {
        this.ketQuaId = owner;
        this.phanCongChamId = target;
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof ChiTietKetQuaId x && Objects.equals(ketQuaId, x.ketQuaId)
                && Objects.equals(phanCongChamId, x.phanCongChamId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(ketQuaId, phanCongChamId);
    }
}
