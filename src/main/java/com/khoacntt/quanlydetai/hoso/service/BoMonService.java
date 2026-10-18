package com.khoacntt.quanlydetai.hoso.service;

import com.khoacntt.quanlydetai.common.exception.KhongTimThayException;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.hoso.dto.BoMonDto;
import com.khoacntt.quanlydetai.hoso.form.BoMonForm;
import com.khoacntt.quanlydetai.hoso.model.BoMon;
import com.khoacntt.quanlydetai.hoso.repository.BoMonRepository;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.*;

@Service
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Transactional(readOnly = true)
public class BoMonService {
    private final BoMonRepository repository;

    public BoMonService(BoMonRepository repository) {
        this.repository = repository;
    }

    public List<BoMonDto> danhSach() {
        return repository.findAll(Sort.by("maBoMon")).stream().map(BoMonDto::tu).toList();
    }

    public BoMonDto chiTiet(Integer id) {
        return BoMonDto.tu(tim(id));
    }

    @Transactional
    public void tao(@Valid BoMonForm form) {
        String ma = form.getMaBoMon().trim().toUpperCase(Locale.ROOT);
        if (repository.existsByMaBoMon(ma)) {
            throw new NghiepVuException("Mã bộ môn đã tồn tại");
        }
        luu(new BoMon(ma, form.getTenBoMon().trim(), (byte) (form.getDangHoatDong() ? 1 : 0)));
    }

    @Transactional
    public void sua(Integer id, @Valid BoMonForm form) {
        BoMon bm = tim(id);
        String ma = form.getMaBoMon().trim().toUpperCase(Locale.ROOT);
        if (repository.existsByMaBoMonAndIdNot(ma, id)) {
            throw new NghiepVuException("Mã bộ môn đã tồn tại");
        }
        bm.capNhat(ma, form.getTenBoMon().trim(), (byte) (form.getDangHoatDong() ? 1 : 0));
        luu(bm);
    }

    @Transactional
    public void xoa(Integer id) {
        var bm = tim(id);
        try { repository.delete(bm); repository.flush(); }
        catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Bộ môn đã được sử dụng; hãy chuyển sang ngừng hoạt động");
        }
    }

    private BoMon tim(Integer id) {
        return repository.findById(id).orElseThrow(() -> new KhongTimThayException("Không tìm thấy bộ môn"));
    }

    private void luu(BoMon bm) {
        try {
            repository.saveAndFlush(bm);
        } catch (DataIntegrityViolationException ex) {
            throw new NghiepVuException("Mã bộ môn đã tồn tại");
        }
    }
}
