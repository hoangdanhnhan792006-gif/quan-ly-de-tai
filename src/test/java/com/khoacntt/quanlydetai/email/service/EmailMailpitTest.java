package com.khoacntt.quanlydetai.email.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import static org.junit.jupiter.api.Assertions.*;

/** Mailpit local riêng để kiểm tra SMTP thật; không gửi thư ra Internet. */
@EnabledIfEnvironmentVariable(named="TEST_SMTP_PORT", matches="[0-9]+")
class EmailMailpitTest {
    @Test void guiHtmlChoBaSinhVienQuaMailpit() {
        var sender = new JavaMailSenderImpl();
        sender.setHost("127.0.0.1");
        sender.setPort(Integer.parseInt(System.getenv("TEST_SMTP_PORT")));
        sender.getJavaMailProperties().put("mail.smtp.connectiontimeout","5000");
        sender.getJavaMailProperties().put("mail.smtp.timeout","5000");
        assertDoesNotThrow(sender::testConnection);
        new EmailService(sender,EmailServiceTest.template(),true,"khoa@example.test","http://localhost:8080")
                .guiThongBaoKetQua(EmailServiceTest.event());
        // Hộp thư Mailpit được kiểm tra độc lập qua API sau khi test chạy.
    }
}
