package com.khoacntt.quanlydetai.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    String xuLyTepQuaLon(Model model) {
        model.addAttribute("message", "Mỗi tệp tối đa 50 MB; tổng một lần nộp tối đa 150 MB.");
        return "error/business";
    }

    @ExceptionHandler(KhongTimThayException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String xuLyKhongTimThay(KhongTimThayException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error/404";
    }

    @ExceptionHandler(NghiepVuException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String xuLyLoiNghiepVu(NghiepVuException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error/business";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String xuLyXungDotDuLieu(DataIntegrityViolationException exception, Model model) {
        LOGGER.warn("Ràng buộc dữ liệu từ chối thao tác", exception);
        model.addAttribute("message", "Dữ liệu đã thay đổi hoặc vi phạm ràng buộc. Vui lòng tải lại trang và thử lại.");
        return "error/business";
    }
}
