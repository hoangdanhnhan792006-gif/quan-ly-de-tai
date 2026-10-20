param([switch]$TenMien, [switch]$NhapLaiMatKhau)

$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $PSScriptRoot
if ($TenMien) {
    $profile = 'local-domain'
    $certificateFile = '.local-https/qldt.nhom4qldt.id.vn.pfx'
    $publicUrl = 'https://qldt.nhom4qldt.id.vn:8443'
} else {
    $profile = 'local-https'
    $certificateFile = '.local-https/localhost.p12'
    $publicUrl = 'https://localhost:8443'
}
$keyStore = Join-Path $projectDir $certificateFile
if (!(Test-Path -LiteralPath $keyStore)) {
    throw "Chua co tep $certificateFile. Xem docs/HTTPS_LOCAL.md."
}
# Biến global chỉ tồn tại trong tiến trình PowerShell hiện tại, không ghi ra file.
if (!(Get-Variable -Name QldtMatKhauPhien -Scope Global -ErrorAction SilentlyContinue)) {
    $global:QldtMatKhauPhien = @{}
}
$dbUsername = if ($env:DB_USERNAME) { $env:DB_USERNAME } else { 'doan_dev' }
$sslCacheKey = "$projectDir|ssl|$keyStore"
$dbCacheKey = "$projectDir|mysql-local|$dbUsername"
if ($NhapLaiMatKhau) {
    $global:QldtMatKhauPhien.Remove($sslCacheKey)
    $global:QldtMatKhauPhien.Remove($dbCacheKey)
}
function Doc-MatKhau([string]$prompt, [string]$cacheKey) {
    if (!$global:QldtMatKhauPhien.ContainsKey($cacheKey)) {
        $secure = Read-Host $prompt -AsSecureString
        if ($secure.Length -eq 0) { throw 'Mat khau khong duoc de trong.' }
        $global:QldtMatKhauPhien[$cacheKey] = $secure
    }
    $secure = $global:QldtMatKhauPhien[$cacheKey]
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
}
$oldSsl = $env:LOCAL_SSL_PASSWORD
$oldDb = $env:DB_PASSWORD
$oldStore = $env:LOCAL_SSL_KEY_STORE
Push-Location $projectDir
try {
    $env:LOCAL_SSL_KEY_STORE = ([System.Uri]$keyStore).AbsoluteUri
    $env:LOCAL_SSL_PASSWORD = Doc-MatKhau 'Nhap mat khau tep P12/PFX da dat khi tao/cap chung chi' $sslCacheKey
    if ([string]::IsNullOrWhiteSpace($env:LOCAL_SSL_PASSWORD)) { throw 'Chua nhap mat khau chung chi.' }
    if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
        $env:DB_PASSWORD = Doc-MatKhau 'Nhap mat khau MySQL local (DB_USERNAME mac dinh doan_dev)' $dbCacheKey
    }
    Write-Host "Chay HTTPS: $publicUrl - MySQL local quan_ly_de_tai."
    Write-Host 'Dung Ctrl+C de dung ung dung.'
    & .\mvnw.cmd "-Dspring-boot.run.profiles=$profile" spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw 'Ung dung dung voi loi. Xem ERROR/Caused by trong log.' }
} finally {
    $env:LOCAL_SSL_PASSWORD = $oldSsl
    $env:DB_PASSWORD = $oldDb
    $env:LOCAL_SSL_KEY_STORE = $oldStore
    Pop-Location
}
