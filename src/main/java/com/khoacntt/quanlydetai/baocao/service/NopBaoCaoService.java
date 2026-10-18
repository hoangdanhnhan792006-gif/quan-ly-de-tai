package com.khoacntt.quanlydetai.baocao.service;
import com.khoacntt.quanlydetai.baocao.form.NopBaoCaoForm;
import com.khoacntt.quanlydetai.baocao.model.LoaiTaiLieuBaoCao;
import com.khoacntt.quanlydetai.common.storage.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import java.util.*;
/**
 * Lưu tệp ngoài transaction; service bên trong kiểm tra lại và commit metadata
 * nguyên bộ.
 */
@Service
@PreAuthorize("hasRole('SINH_VIEN')")
public class NopBaoCaoService {
    private final LuuTepService storage;
    private final BaoCaoService baoCao;
    public NopBaoCaoService(LuuTepService storage, BaoCaoService baoCao) {
        this.storage = storage;
        this.baoCao = baoCao;
    }
    public Integer nop(Integer dkId, NopBaoCaoForm f) {
        baoCao.kiemTraNop(dkId);
        Map<LoaiTaiLieuBaoCao, TepDaLuu> files = new EnumMap<>(LoaiTaiLieuBaoCao.class);
        try {
            files.put(LoaiTaiLieuBaoCao.BAO_CAO_PDF, storage.luu(f.getPdf(), "bao-cao", Set.of("pdf")));
            files.put(LoaiTaiLieuBaoCao.SLIDE, storage.luu(f.getSlide(), "bao-cao", Set.of("pdf", "pptx")));
            files.put(LoaiTaiLieuBaoCao.MA_NGUON, storage.luu(f.getSource(), "bao-cao", Set.of("zip")));
            return baoCao.nop(dkId, f.getGhiChu(), files);
        } catch (RuntimeException ex) {
            files.values().forEach(storage::xoaTepMoi);
            throw ex;
        }
    }
}
