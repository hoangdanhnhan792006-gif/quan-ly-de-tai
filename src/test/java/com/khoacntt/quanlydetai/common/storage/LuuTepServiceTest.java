package com.khoacntt.quanlydetai.common.storage;

import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;

class LuuTepServiceTest {
    @TempDir
    Path root;
    LuuTepService service;
    @BeforeEach
    void setup() {
        service = new LuuTepService(root.toString());
    }
    MockMultipartFile file(String name, byte[] bytes) {
        return new MockMultipartFile("tep", name, "text/html", bytes);
    }
    byte[] zip(String entry) throws IOException {
        var bytes = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry(entry));
            zip.write("noi dung".getBytes());
            zip.closeEntry();
        }
        return bytes.toByteArray();
    }
    @Test
    void luuTenNgauNhienDocVaDonTep() throws Exception {
        byte[] bytes = "%PDF-1.4\n%%EOF".getBytes();
        var f = service.luu(file("../../bao-cao.pdf", bytes), "bao-cao", Set.of("pdf"));
        assertEquals("bao-cao.pdf", f.tenGoc());
        assertEquals("application/pdf", f.mime());
        assertEquals(64, f.sha256().length());
        assertArrayEquals(bytes, service.doc(f.khoa()).getContentAsByteArray());
        service.xoaTepMoi(f);
        assertThrows(NghiepVuException.class, () -> service.doc(f.khoa()));
    }
    @Test
    void tuChoiGiaMaoDuoiVaDuongDan() {
        assertThrows(NghiepVuException.class,
                () -> service.luu(file("virus.pdf", "<html>malware</html>".getBytes()), "bao-cao", Set.of("pdf")));
        assertThrows(NghiepVuException.class,
                () -> service.luu(file("x.exe", new byte[]{1}), "bao-cao", Set.of("pdf")));
        assertThrows(NghiepVuException.class, () -> service.doc("../application.yml"));
    }
    @Test
    void zipTraversalVaOfficeGiaKhongDuocLuu() throws Exception {
        byte[] bad = zip("../secret.txt"), fakeOffice = zip("readme.txt");
        assertThrows(NghiepVuException.class, () -> service.luu(file("x.zip", bad), "bao-cao", Set.of("zip")));
        assertThrows(NghiepVuException.class, () -> service.luu(file("x.pptx", fakeOffice), "bao-cao", Set.of("pptx")));
        try (var list = Files.list(root)) {
            assertEquals(0, list.count());
        }
    }
    @Test
    void tepRongVaQuaKichThuocKhongDuocLuu() {
        assertThrows(NghiepVuException.class, () -> service.luu(file("x.pdf", new byte[0]), "bao-cao", Set.of("pdf")));
        var huge = new MockMultipartFile("tep", "large.pdf", "application/pdf", new byte[]{1}) {
            @Override
            public long getSize() {
                return 51L * 1024 * 1024;
            }
        };
        assertThrows(NghiepVuException.class, () -> service.luu(huge, "bao-cao", Set.of("pdf")));
    }
    @Test
    void chapNhanSourceZipVaOfficeDungCauTruc() throws Exception {
        var source = service.luu(file("code.zip", zip("src/Main.java")), "bao-cao", Set.of("zip"));
        assertEquals("application/zip", source.mime());
        var out = new ByteArrayOutputStream();
        try (var z = new ZipOutputStream(out)) {
            for (String n : List.of("[Content_Types].xml", "ppt/presentation.xml")) {
                z.putNextEntry(new ZipEntry(n));
                z.write("<xml/>".getBytes());
                z.closeEntry();
            }
        }
        var slides = service.luu(file("slides.pptx", out.toByteArray()), "bao-cao", Set.of("pptx"));
        assertTrue(slides.mime().contains("presentation"));
    }
}
