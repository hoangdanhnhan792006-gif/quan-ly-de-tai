package com.khoacntt.quanlydetai.email.service;

import com.khoacntt.quanlydetai.email.dto.ThongBaoKetQua;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {
    static TemplateEngine template() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML"); resolver.setCharacterEncoding("UTF-8");
        var engine = new org.thymeleaf.spring6.SpringTemplateEngine(); engine.setTemplateResolver(resolver); return engine;
    }
    static ThongBaoKetQua event() {
        return new ThongBaoKetQua(1, 2, "Đề tài <script>test</script>", "Đợt KLTN", List.of("sv1@example.test", "sv2@example.test", "sv3@example.test"));
    }
    static JavaMailSender sender() {
        var sender = mock(JavaMailSender.class);
        when(sender.createMimeMessage()).thenAnswer(i -> new MimeMessage(Session.getInstance(new Properties())));
        return sender;
    }
    @Test void moiSinhVienNhanMotThuHtmlUtf8KhongLoDanhSachNguoiNhan() throws Exception {
        var sender=sender();
        new EmailService(sender,template(),true,"khoa@example.test","https://example.test/").guiThongBaoKetQua(event());
        var captor=org.mockito.ArgumentCaptor.forClass(MimeMessage.class);
        verify(sender,times(3)).send(captor.capture());
        for (int i=0;i<3;i++) {
            var message=captor.getAllValues().get(i);
            message.saveChanges();
            assertEquals(1,message.getAllRecipients().length);
            assertEquals(event().nguoiNhans().get(i),message.getAllRecipients()[0].toString());
            assertTrue(message.getContentType().toLowerCase().contains("text/html"));
            String html=(String)message.getContent();
            assertTrue(html.contains("Đợt KLTN"));
            assertTrue(html.contains("&lt;script&gt;"));
            assertFalse(html.contains("<script>"));
            assertTrue(html.contains("https://example.test/sinh-vien/ket-qua"));
        }
    }
    @Test void loiMotThuVanThuGuiChoCacThanhVienConLai() {
        var sender=sender();
        doThrow(new MailSendException("SMTP offline")).doNothing().when(sender).send(any(MimeMessage.class));
        assertDoesNotThrow(() -> new EmailService(sender,template(),true,"khoa@example.test","http://localhost:8080").guiThongBaoKetQua(event()));
        verify(sender,times(3)).send(any(MimeMessage.class));
    }
    @Test void tatEmailKhongKetNoiSmtp() {
        var sender=sender();
        new EmailService(sender,template(),false,"khoa@example.test","http://localhost:8080").guiThongBaoKetQua(event());
        verify(sender,never()).send(any(MimeMessage.class));
    }
}
