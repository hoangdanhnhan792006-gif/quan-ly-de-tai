package com.khoacntt.quanlydetai.danhgia.controller;
import com.khoacntt.quanlydetai.baocao.service.BaoCaoService;
import com.khoacntt.quanlydetai.common.exception.NghiepVuException;
import com.khoacntt.quanlydetai.danhgia.form.*;
import com.khoacntt.quanlydetai.danhgia.service.*;
import com.khoacntt.quanlydetai.detai.service.DeTaiService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@PreAuthorize("hasAnyRole('GIANG_VIEN','TRUONG_KHOA','CAN_BO_KHOA')")
public class DanhGiaController {
    private final DanhGiaService service;
    private final DanhGiaTruyVanService query;
    private final HoiDongService hoiDongs;
    private final BaoCaoService baoCaos;
    private final DeTaiService deTais;
    private final ChamDiemService cham;
    private final KetQuaService ketQua;
    public DanhGiaController(DanhGiaService service, DanhGiaTruyVanService query, HoiDongService hoiDongs,
            BaoCaoService baoCaos, DeTaiService deTais, ChamDiemService cham, KetQuaService ketQua) {
        this.service = service;
        this.query = query;
        this.hoiDongs = hoiDongs;
        this.baoCaos = baoCaos;
        this.deTais = deTais;
        this.cham = cham;
        this.ketQua = ketQua;
    }
    @GetMapping("/danh-gia")
    public String danhSach(Model m) {
        m.addAttribute("danhSach", query.danhSach());
        return "danhgia/danh-sach";
    }
    @GetMapping("/danh-gia/{id}")
    public String chiTiet(@PathVariable Integer id, Model m) {
        m.addAttribute("danhGia", query.chiTiet(id));
        return "danhgia/chi-tiet";
    }
    @GetMapping("/khoa/danh-gia/tao")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public String tao(Model m) {
        m.addAttribute("form", new KeHoachDanhGiaForm());
        luaChon(m);
        return "danhgia/ke-hoach";
    }
    @GetMapping("/khoa/danh-gia/{id}/sua")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public String sua(@PathVariable Integer id, Model m) {
        m.addAttribute("form", service.formSua(id));
        m.addAttribute("id", id);
        luaChon(m);
        return "danhgia/ke-hoach";
    }
    @PostMapping("/khoa/danh-gia/tao")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public String tao(@Valid @ModelAttribute("form") KeHoachDanhGiaForm f, BindingResult e, Model m,
            RedirectAttributes r) {
        if (!e.hasErrors())
            try {
                Integer id = service.lap(f);
                r.addFlashAttribute("thanhCong", "Đã lập kế hoạch đánh giá");
                return ve(id);
            } catch (NghiepVuException ex) {
                e.reject("nghiepVu", ex.getMessage());
            }
        luaChon(m);
        return "danhgia/ke-hoach";
    }
    @PostMapping("/khoa/danh-gia/{id}/sua")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public String sua(@PathVariable Integer id, @Valid @ModelAttribute("form") KeHoachDanhGiaForm f, BindingResult e,
            Model m, RedirectAttributes r) {
        if (!e.hasErrors())
            try {
                service.sua(id, f);
                r.addFlashAttribute("thanhCong", "Đã sửa kế hoạch");
                return ve(id);
            } catch (NghiepVuException ex) {
                e.reject("nghiepVu", ex.getMessage());
            }
        m.addAttribute("id", id);
        luaChon(m);
        return "danhgia/ke-hoach";
    }
    @PostMapping("/khoa/danh-gia/{id}/phan-bien")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public String phanBien(@PathVariable Integer id, RedirectAttributes r) {
        thucHien(() -> service.batDauPhanBien(id), r, "Đã chốt bản báo cáo cuối và mở phản biện");
        return ve(id);
    }
    @PostMapping("/khoa/danh-gia/{id}/hoi-dong")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public String hoiDong(@PathVariable Integer id, RedirectAttributes r) {
        thucHien(() -> service.batDauHoiDong(id), r, "Đã mở chấm hội đồng");
        return ve(id);
    }
    @PostMapping("/giang-vien/cham-diem/{id}")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public String cham(@PathVariable Integer id, @Valid @ModelAttribute PhieuChamForm f, BindingResult e,
            RedirectAttributes r) {
        Integer dg = query.danhGiaCuaPhanCong(id);
        if (e.hasErrors())
            r.addFlashAttribute("loi", "Điểm từ 0 đến 10, tối đa 2 chữ số thập phân; nhận xét tối đa 10000 ký tự");
        else
            thucHien(() -> cham.luu(id, f), r,
                    Boolean.TRUE.equals(f.getNop()) ? "Đã nộp phiếu chấm" : "Đã lưu phiếu nháp");
        return ve(dg);
    }
    @PostMapping("/giang-vien/cham-diem/{id}/xin-sua")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public String xinSua(@PathVariable Integer id, @Valid @ModelAttribute YeuCauSuaDiemForm f, BindingResult e,
            RedirectAttributes r) {
        Integer dg = query.danhGiaCuaPhanCong(id);
        if (e.hasErrors())
            r.addFlashAttribute("loi", "Cần lý do sửa điểm, tối đa 5000 ký tự");
        else
            thucHien(() -> cham.yeuCauSua(id, f), r, "Đã gửi yêu cầu sửa điểm");
        return ve(dg);
    }
    @PostMapping("/khoa/sua-diem/{id}/duyet")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public String duyetSua(@PathVariable Integer id, @Valid @ModelAttribute DuyetSuaDiemForm f, BindingResult e,
            RedirectAttributes r) {
        Integer dg = query.danhGiaCuaYeuCau(id);
        if (e.hasErrors())
            r.addFlashAttribute("loi", "Kiểm tra lựa chọn duyệt và nhận xét tối đa 5000 ký tự");
        else
            thucHien(() -> cham.duyetYeuCau(id, f), r, "Đã xử lý yêu cầu sửa điểm");
        return ve(dg);
    }
    @PostMapping("/giang-vien/cham-diem/yeu-cau/{id}/huy")
    @PreAuthorize("hasRole('GIANG_VIEN')")
    public String huySua(@PathVariable Integer id, RedirectAttributes r) {
        Integer dg = query.danhGiaCuaYeuCau(id);
        thucHien(() -> cham.huyYeuCau(id), r, "Đã hủy yêu cầu");
        return ve(dg);
    }
    @PostMapping("/danh-gia/{id}/tong-hop")
    public String tongHop(@PathVariable Integer id, @Valid @ModelAttribute TongHopForm f, BindingResult e,
            RedirectAttributes r) {
        if (e.hasErrors())
            r.addFlashAttribute("loi", "Cần nhận xét tổng hợp, tối đa 10000 ký tự");
        else
            thucHien(() -> ketQua.tongHop(id, f), r, "Đã tạo phiên bản kết quả chờ khoa xác nhận");
        return ve(id);
    }
    @PostMapping("/khoa/ket-qua/{id}/xac-nhan")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public String xacNhan(@PathVariable Integer id, RedirectAttributes r) {
        Integer dg = ketQua.danhGiaCuaKetQua(id);
        thucHien(() -> ketQua.xacNhan(id), r, "Đã xác nhận kết quả");
        return ve(dg);
    }
    @PostMapping("/khoa/ket-qua/{id}/cong-bo")
    @PreAuthorize("hasAnyRole('TRUONG_KHOA','CAN_BO_KHOA')")
    public String congBo(@PathVariable Integer id, RedirectAttributes r) {
        Integer dg = ketQua.danhGiaCuaKetQua(id);
        thucHien(() -> ketQua.congBo(id), r, "Đã công bố kết quả");
        return ve(dg);
    }
    @PostMapping("/danh-gia/ket-qua/{id}/huy")
    public String huyKetQua(@PathVariable Integer id, RedirectAttributes r) {
        Integer dg = ketQua.danhGiaCuaKetQua(id);
        thucHien(() -> ketQua.huy(id), r, "Đã hủy bản chưa công bố");
        return ve(dg);
    }
    private void luaChon(Model m) {
        m.addAttribute("dangKys", baoCaos.danhSach());
        m.addAttribute("giangViens", deTais.giangVienHuongDan());
        m.addAttribute("hoiDongs", hoiDongs.danhSach());
    }
    private String ve(Integer id) {
        return "redirect:/danh-gia/" + id;
    }
    private void thucHien(Runnable action, RedirectAttributes r, String thanhCong) {
        try {
            action.run();
            r.addFlashAttribute("thanhCong", thanhCong);
        } catch (NghiepVuException ex) {
            r.addFlashAttribute("loi", ex.getMessage());
        }
    }
}
