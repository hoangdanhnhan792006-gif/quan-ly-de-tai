package com.khoacntt.quanlydetai.common.storage;

import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;

@Service
public class LuuTepService {
    private static final int MAX_BYTES = 50 * 1024 * 1024;
    private final Path root;
    public LuuTepService(@Value("${app.storage.root:uploads}") String folder) {
        this.root = Path.of(folder).toAbsolutePath().normalize();
    }
    public TepDaLuu luu(MultipartFile file, String namespace, Set<String> duoiChoPhep) {
        if (!Set.of("bao-cao", "thong-bao").contains(namespace))
            throw new IllegalArgumentException("Namespace không hợp lệ");
        if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES)
            throw new NghiepVuException("Tệp phải có nội dung và không vượt quá 50 MB");
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "_");
        if (name.isBlank() || name.length() > 255)
            throw new NghiepVuException("Tên tệp không hợp lệ (tối đa 255 ký tự)");
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!duoiChoPhep.contains(ext))
            throw new NghiepVuException(
                    "Định dạng tệp không được hỗ trợ: " + String.join(", ", new TreeSet<>(duoiChoPhep)));
        Path target = null;
        try (InputStream in = file.getInputStream()) {
            byte[] data = in.readNBytes(MAX_BYTES + 1);
            if (data.length == 0 || data.length > MAX_BYTES)
                throw new NghiepVuException("Tệp rỗng hoặc vượt quá 50 MB");
            String mime = kiemTraNoiDung(data, ext);
            String key = namespace + "/" + UUID.randomUUID();
            target = duongDan(key);
            Files.createDirectories(target.getParent());
            if (Files.isSymbolicLink(root) || Files.isSymbolicLink(target.getParent()))
                throw new IOException("Không dùng symlink");
            Files.write(target, data, StandardOpenOption.CREATE_NEW);
            String sha = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
            return new TepDaLuu(key, name, mime, data.length, sha);
        } catch (NghiepVuException ex) {
            throw ex;
        } catch (Exception ex) {
            if (target != null) {
                try {
                    Files.deleteIfExists(target);
                } catch (IOException ignored) {
                }
            }
            throw new NghiepVuException("Không lưu được tệp; vui lòng thử lại");
        }
    }
    private String kiemTraNoiDung(byte[] data, String ext) throws IOException {
        if (ext.equals("pdf")) {
            if (data.length < 8 || !new String(data, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-"))
                throw new NghiepVuException("Nội dung tệp không phải PDF");
            return "application/pdf";
        }
        if (data.length < 4 || data[0] != 'P' || data[1] != 'K')
            throw new NghiepVuException("Tệp không phải ZIP/Office hợp lệ");
        Set<String> names = new HashSet<>();
        long total = 0;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry e;
            byte[] buffer = new byte[8192];
            while ((e = zip.getNextEntry()) != null) {
                String n = e.getName().replace('\\', '/');
                if (n.startsWith("/") || n.contains("../") || n.contains(":") || !names.add(n) || names.size() > 10000)
                    throw new NghiepVuException("Cấu trúc ZIP không hợp lệ");
                int count;
                while ((count = zip.read(buffer)) != -1) {
                    total += count;
                    if (total > 200L * 1024 * 1024)
                        throw new NghiepVuException("Nội dung giải nén vượt quá 200 MB");
                }
            }
        } catch (ZipException ex) {
            throw new NghiepVuException("Tệp ZIP/Office bị hỏng");
        }
        if (names.isEmpty() || total == 0)
            throw new NghiepVuException("Tệp ZIP/Office không có nội dung");
        if (ext.equals("pptx") && (!names.contains("[Content_Types].xml") || !names.contains("ppt/presentation.xml")))
            throw new NghiepVuException("Nội dung tệp không phải PowerPoint PPTX");
        if (ext.equals("docx") && (!names.contains("[Content_Types].xml") || !names.contains("word/document.xml")))
            throw new NghiepVuException("Nội dung tệp không phải Word DOCX");
        return switch (ext) {
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "application/zip";
        };
    }
    private Path duongDan(String key) {
        if (key == null || !key.matches("(bao-cao|thong-bao)/[0-9a-f-]{36}"))
            throw new NghiepVuException("Khóa tệp không hợp lệ");
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root))
            throw new NghiepVuException("Đường dẫn tệp không hợp lệ");
        return path;
    }
    public Resource doc(String key) {
        Path path = duongDan(key);
        if (Files.isSymbolicLink(root) || Files.isSymbolicLink(path.getParent())
                || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
            throw new NghiepVuException("Tệp không còn trên máy chủ; liên hệ khoa");
        return new FileSystemResource(path);
    }
    public void xoaTepMoi(TepDaLuu file) {
        try {
            Files.deleteIfExists(duongDan(file.khoa()));
        } catch (IOException ex) {
            org.slf4j.LoggerFactory.getLogger(getClass()).warn("Chưa dọn được tệp upload thất bại {}", file.khoa());
        }
    }
}
