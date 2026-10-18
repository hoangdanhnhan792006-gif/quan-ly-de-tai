package com.khoacntt.quanlydetai.email.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(EmailTransactionTest.Config.class)
class EmailTransactionTest {
    @Configuration @EnableAsync
    static class Config {
        @Bean JavaMailSender sender() { return EmailServiceTest.sender(); }
        @Bean EmailService email(JavaMailSender sender) {
            return new EmailService(sender,EmailServiceTest.template(),true,"khoa@example.test","http://localhost:8080");
        }
        @Bean(name="taskExecutor",destroyMethod="shutdown") java.util.concurrent.ExecutorService executor() {
            return Executors.newSingleThreadExecutor();
        }
        @Bean org.springframework.transaction.event.TransactionalEventListenerFactory listeners() {
            return new org.springframework.transaction.event.TransactionalEventListenerFactory();
        }
    }
    @Autowired ApplicationEventPublisher publisher;
    @Autowired JavaMailSender sender;
    @Autowired ExecutorService executor;
    private TransactionTemplate transaction() {
        return new TransactionTemplate(new AbstractPlatformTransactionManager() {
            protected Object doGetTransaction() { return new Object(); }
            protected void doBegin(Object tx, TransactionDefinition def) {}
            protected void doCommit(DefaultTransactionStatus status) {}
            protected void doRollback(DefaultTransactionStatus status) {}
        });
    }
    @BeforeEach void resetCalls() { clearInvocations(sender); }
    @Test void rollbackKhongGuiEmail() throws Exception {
        transaction().executeWithoutResult(status -> {
            publisher.publishEvent(EmailServiceTest.event());
            status.setRollbackOnly();
        });
        executor.submit(() -> {}).get(5,TimeUnit.SECONDS);
        verify(sender,never()).send(any(MimeMessage.class));
    }
    @Test void chiGuiSauCommitVaChayTrenLuongNen() throws Exception {
        String requestThread=Thread.currentThread().getName();
        var sent=new CountDownLatch(3);
        var threadNames=new ConcurrentLinkedQueue<String>();
        doAnswer(i -> { threadNames.add(Thread.currentThread().getName()); sent.countDown(); return null; })
                .when(sender).send(any(MimeMessage.class));
        transaction().executeWithoutResult(status -> {
            publisher.publishEvent(EmailServiceTest.event());
            verify(sender,never()).send(any(MimeMessage.class));
        });
        assertTrue(sent.await(5,TimeUnit.SECONDS));
        assertTrue(threadNames.stream().noneMatch(requestThread::equals));
        verify(sender,times(3)).send(any(MimeMessage.class));
    }
}
