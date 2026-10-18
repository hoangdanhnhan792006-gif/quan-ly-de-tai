package com.khoacntt.quanlydetai.common.storage;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import java.nio.charset.StandardCharsets;
public final class TaiTepResponse {
    private TaiTepResponse() {
    }
    public static ResponseEntity<Resource> tao(TaiTepDto tep, Resource resource) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(tep.mime()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(tep.tenGoc(), StandardCharsets.UTF_8).build()
                                .toString())
                .header("X-Content-Type-Options", "nosniff").cacheControl(CacheControl.noStore())
                .contentLength(tep.kichThuoc()).body(resource);
    }
}
