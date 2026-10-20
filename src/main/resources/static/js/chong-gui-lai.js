// Chống bấm gửi hai lần cho form có thuộc tính data-chong-gui-lai (nộp báo cáo, thêm tệp thông báo...).
// Mỗi lần gửi tạo một bản ghi mới; sự kiện submit chỉ chạy khi trình duyệt đã cho gửi (đủ ô required), nên thiếu tệp thì nút không bị khóa.
(function () {
    var form = document.querySelector('form[data-chong-gui-lai]');
    if (!form) {
        return;
    }
    var nut = form.querySelector('button[type="submit"]');
    if (!nut) {
        return;
    }
    var chuGoc = nut.textContent;

    form.addEventListener('submit', function () {
        nut.disabled = true;
        nut.textContent = 'Đang tải lên…';
    });

    // Bấm Back quay lại trang: trình duyệt có thể lấy trang từ bfcache với nút vẫn đang khóa, nên mở lại.
    window.addEventListener('pageshow', function (e) {
        if (e.persisted) {
            nut.disabled = false;
            nut.textContent = chuGoc;
        }
    });
})();
