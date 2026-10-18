package com.khoacntt.quanlydetai.email.service;

import com.khoacntt.quanlydetai.email.dto.ThongBaoKetQua;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final boolean enabled;
    private final String from;
    private final String publicUrl;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine,
            @Value("${app.mail.enabled:false}") boolean enabled,
            @Value("${app.mail.from}") String from,
            @Value("${app.public-url}") String publicUrl) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.enabled = enabled;
        this.from = from;
        this.publicUrl = publicUrl.replaceAll("/+$", "");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void guiThongBaoKetQua(ThongBaoKetQua thongBao) {
        if (!enabled) {
            log.info("[EMAIL] Đang tắt gửi thư; kết quả {} đã công bố", thongBao.ketQuaId());
            return;
        }
        Context context = new Context();
        context.setVariable("thongBao", thongBao);
        context.setVariable("lienKet", publicUrl + "/sinh-vien/ket-qua");
        final String html;
        try {
            html = templateEngine.process("mail/ket-qua-de-tai", context);
        } catch (org.thymeleaf.exceptions.TemplateEngineException ex) {
            log.error("[EMAIL] Không dựng được mẫu thư cho kết quả {}", thongBao.ketQuaId());
            return;
        }
        for (String recipient : thongBao.nguoiNhans()) {
            try {
                var message = mailSender.createMimeMessage();
                var helper = new MimeMessageHelper(message, false, "UTF-8");
                helper.setFrom(from);
                helper.setTo(recipient); // Mỗi SV một thư, không lộ danh sách người nhận.
                helper.setSubject("Khoa CNTT - Công bố kết quả đề tài");
                helper.setText(html, true);
                mailSender.send(message);
                log.info("[EMAIL] Đã gửi một thư thông báo cho kết quả {}", thongBao.ketQuaId());
            } catch (MessagingException | MailException ex) {
                // Lỗi một người nhận không ngăn gửi cho các thành viên còn lại.
                // Không ghi credential SMTP hoặc nội dung lỗi chứa thông tin nhạy cảm.
                log.warn("[EMAIL] Gửi một thư thất bại cho kết quả {}; kiểm tra SMTP/Mailpit", thongBao.ketQuaId());
            }
        }
    }
}
