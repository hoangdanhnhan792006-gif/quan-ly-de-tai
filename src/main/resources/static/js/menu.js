// Menu điện thoại và đánh dấu mục đang mở.
// Chỉ đổi giao diện: mục nào hiện ra do th:if ở server quyết định.
(function () {
    var nav = document.querySelector('nav.sidebar');
    if (!nav) {
        return;
    }
    var nut = nav.querySelector('.menu-toggle');
    var panel = nav.querySelector('.sidebar-panel');

    function datTrangThai(mo) {
        nav.classList.toggle('menu-mo', mo);
        nut.setAttribute('aria-expanded', mo ? 'true' : 'false');
    }

    if (nut && panel) {
        nut.addEventListener('click', function () {
            datTrangThai(!nav.classList.contains('menu-mo'));
        });
        // Đóng khi bấm Esc, bấm ra ngoài panel (lớp phủ) hoặc chọn một link
        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && nav.classList.contains('menu-mo')) {
                datTrangThai(false);
                nut.focus();
            }
        });
        document.addEventListener('click', function (e) {
            if (nav.classList.contains('menu-mo') && !panel.contains(e.target) && !nut.contains(e.target)) {
                datTrangThai(false);
            }
        });
        panel.addEventListener('click', function (e) {
            if (e.target.closest('a')) {
                datTrangThai(false);
            }
        });
    }

    // Link có đường dẫn khớp dài nhất với trang hiện tại được đánh dấu aria-current="page".
    // Ví dụ /de-tai/cua-toi khớp "Đề tài của tôi", không khớp "Đề tài".
    var hienTai = location.pathname;
    var tot = null;
    var doDai = -1;
    nav.querySelectorAll('.sidebar-panel a[href]').forEach(function (a) {
        var p = new URL(a.href, location.href).pathname;
        var khop = hienTai === p || hienTai.indexOf(p.replace(/\/$/, '') + '/') === 0;
        if (khop && p.length > doDai) {
            tot = a;
            doDai = p.length;
        }
    });
    if (tot) {
        tot.setAttribute('aria-current', 'page');
    }

    // Thanh tab chuyển chế độ trong trang (ví dụ /de-tai, /de-tai/cua-toi): chỉ đánh dấu tab khớp đúng đường dẫn.
    document.querySelectorAll('.tabs a[href]').forEach(function (a) {
        if (new URL(a.href, location.href).pathname === hienTai) {
            a.setAttribute('aria-current', 'page');
        }
    });
})();
