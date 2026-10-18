package com.khoacntt.quanlydetai.baocao.service;

import com.khoacntt.quanlydetai.baocao.form.NopBaoCaoForm;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.common.storage.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NopBaoCaoServiceTest {
    @Mock LuuTepService storage;
    @Mock BaoCaoService metadata;
    @InjectMocks NopBaoCaoService service;
    NopBaoCaoForm form;
    TepDaLuu pdf=new TepDaLuu("bao-cao/a","a.pdf","application/pdf",10,"hash");
    TepDaLuu slide=new TepDaLuu("bao-cao/b","b.pdf","application/pdf",10,"hash");
    TepDaLuu source=new TepDaLuu("bao-cao/c","c.zip","application/zip",10,"hash");
    @BeforeEach void setup() {
        form=new NopBaoCaoForm();
        form.setPdf(new MockMultipartFile("pdf",new byte[]{1}));
        form.setSlide(new MockMultipartFile("slide",new byte[]{2}));
        form.setSource(new MockMultipartFile("source",new byte[]{3}));
    }
    @Test void metadataRollbackThiDonDuBaTep() {
        when(storage.luu(any(),anyString(),anySet())).thenReturn(pdf,slide,source);
        when(metadata.nop(eq(1),isNull(),anyMap())).thenThrow(new NghiepVuException("Hết hạn trong lúc upload"));
        assertThrows(NghiepVuException.class,()->service.nop(1,form));
        verify(storage).xoaTepMoi(pdf);verify(storage).xoaTepMoi(slide);verify(storage).xoaTepMoi(source);
    }
    @Test void tepThuBaSaiThiKhongLuuMetadataVaDonHaiTepTruoc() {
        when(storage.luu(any(),anyString(),anySet())).thenReturn(pdf,slide).thenThrow(new NghiepVuException("ZIP sai"));
        assertThrows(NghiepVuException.class,()->service.nop(1,form));
        verify(metadata,never()).nop(any(),any(),anyMap());
        verify(storage).xoaTepMoi(pdf);verify(storage).xoaTepMoi(slide);
    }
    @Test void khongDuQuyenKhongGhiTep() {
        doThrow(new org.springframework.security.access.AccessDeniedException("Không phải trưởng nhóm")).when(metadata).kiemTraNop(1);
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.nop(1,form));
        verifyNoInteractions(storage);
    }
    @Test void commitThanhCongKhongXoaTep() {
        when(storage.luu(any(),anyString(),anySet())).thenReturn(pdf,slide,source);
        when(metadata.nop(eq(1),isNull(),anyMap())).thenReturn(12);
        assertEquals(12,service.nop(1,form));
        verify(storage,never()).xoaTepMoi(any());
    }
}
