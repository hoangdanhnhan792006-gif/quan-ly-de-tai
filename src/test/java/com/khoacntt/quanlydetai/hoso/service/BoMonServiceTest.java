package com.khoacntt.quanlydetai.hoso.service;

import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.hoso.form.BoMonForm;
import com.khoacntt.quanlydetai.hoso.model.BoMon;
import com.khoacntt.quanlydetai.hoso.repository.BoMonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class BoMonServiceTest {
    @Mock BoMonRepository repository;
    @InjectMocks BoMonService service;
    BoMonForm form() { var f=new BoMonForm();f.setMaBoMon("cnpm");f.setTenBoMon("Công nghệ phần mềm");return f; }
    @Test void taoChuanHoaMa() {
        service.tao(form());var cap=ArgumentCaptor.forClass(BoMon.class);verify(repository).saveAndFlush(cap.capture());
        assertThat(cap.getValue().getMaBoMon()).isEqualTo("CNPM");
    }
    @Test void trungMaBiChan() {
        when(repository.existsByMaBoMon("CNPM")).thenReturn(true);
        assertThatThrownBy(()->service.tao(form())).isInstanceOf(NghiepVuException.class);
    }
    @Test void ngungHoatDongKhongXoaBoMon() {
        var b=new BoMon("CNPM","Công nghệ phần mềm",(byte)1);when(repository.findById(1)).thenReturn(Optional.of(b));
        var f=form();f.setDangHoatDong(false);service.sua(1,f);assertThat(b.getDangHoatDong()).isZero();
        verify(repository,never()).delete(any());
    }
}
