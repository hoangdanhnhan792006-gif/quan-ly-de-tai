package com.khoacntt.quanlydetai.thongbao.service;
import com.khoacntt.quanlydetai.common.storage.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Set;
@Service
@PreAuthorize("hasAnyRole('ADMIN','TRUONG_KHOA','CAN_BO_KHOA')")
public class TepThongBaoService {
    private final ThongBaoService service;
    private final LuuTepService storage;
    public TepThongBaoService(ThongBaoService service, LuuTepService storage) {
        this.service = service;
        this.storage = storage;
    }
    public void them(Integer id, MultipartFile file) {
        service.kiemTraThemTep(id);
        var tep = storage.luu(file, "thong-bao", Set.of("pdf", "pptx", "docx", "zip"));
        try {
            service.themTep(id, tep);
        } catch (RuntimeException ex) {
            storage.xoaTepMoi(tep);
            throw ex;
        }
    }
}
