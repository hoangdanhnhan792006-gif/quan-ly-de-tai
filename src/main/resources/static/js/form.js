// Dùng chung cho các form: hiện/ẩn mật khẩu và chặn trường bắt buộc chỉ có dấu cách.
document.querySelectorAll('input[type="password"]').forEach((input) => {
    const wrapper = document.createElement('div');
    wrapper.className = 'password-field';
    input.before(wrapper);
    wrapper.append(input);

    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'password-toggle';
    button.setAttribute('aria-controls', input.id);
    const eye = '<svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>';
    const eyeOff = '<svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2"><path d="m3 3 18 18M10 5a12 12 0 0 1 2 0c6 0 10 7 10 7a19 19 0 0 1-4 4M6 6a20 20 0 0 0-4 6s4 7 10 7a12 12 0 0 0 5-1M10 10a3 3 0 0 0 4 4"/></svg>';
    const update = () => {
        const visible = input.type === 'text';
        button.innerHTML = visible ? eyeOff : eye; // SVG cố định, không lấy nội dung từ người dùng.
        button.setAttribute('aria-label', visible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu');
        button.setAttribute('aria-pressed', String(visible));
        button.title = visible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu';
    };
    button.addEventListener('click', () => {
        input.type = input.type === 'password' ? 'text' : 'password';
        update();
    });
    input.form?.addEventListener('submit', () => { input.type = 'password'; update(); });
    wrapper.append(button);
    update();
});

// Input không khai báo type vẫn là ô text theo mặc định của HTML.
document.querySelectorAll('input[required][type="text"], input[required]:not([type]), textarea[required]').forEach((input) => {
    const validate = () => input.setCustomValidity(input.value && !input.value.trim()
        ? 'Vui lòng nhập nội dung, không chỉ nhập dấu cách.' : '');
    input.addEventListener('input', validate);
    validate();
});

const newPassword = document.getElementById('matKhauMoi');
const confirmation = document.getElementById('xacNhanMatKhau');
if (newPassword && confirmation) {
    const validatePair = () => {
        const hasValue = newPassword.value.length > 0 || confirmation.value.length > 0;
        // Hai ô mật khẩu trong form sửa tài khoản là tùy chọn khi cả hai cùng trống.
        if (newPassword.dataset.optional === 'true') {
            newPassword.required = hasValue;
            confirmation.required = hasValue;
        }
        confirmation.setCustomValidity(confirmation.value && confirmation.value !== newPassword.value
            ? 'Xác nhận mật khẩu mới không khớp.' : '');
    };
    newPassword.addEventListener('input', validatePair);
    confirmation.addEventListener('input', validatePair);
    validatePair();
}
