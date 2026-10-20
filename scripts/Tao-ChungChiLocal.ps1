# Chạy bằng Windows PowerShell tại thư mục project; không cần quyền Administrator.
$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $PSScriptRoot
$certDir = Join-Path $projectDir '.local-https'
$keyStore = Join-Path $certDir 'localhost.p12'
$publicCert = Join-Path $certDir 'localhost.cer'
if (Test-Path -LiteralPath $keyStore) {
    throw 'Da co localhost.p12. Khong ghi de. Dung Chay-HttpsLocal.ps1 de chay voi mat khau da dat.'
}
$keytoolCommand = Get-Command keytool.exe -ErrorAction SilentlyContinue
if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin/keytool.exe'))) {
    $keytool = Join-Path $env:JAVA_HOME 'bin/keytool.exe'
} elseif ($keytoolCommand) {
    $keytool = $keytoolCommand.Source
} else {
    throw 'Chua tim thay keytool. Dat JAVA_HOME den JDK 21 truoc khi chay.'
}
$password = Read-Host 'Dat mat khau chung chi local (toi thieu 8 ky tu, luu lai de dung lan sau)' -AsSecureString
$confirmation = Read-Host 'Nhap lai mat khau chung chi' -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
$ptr2 = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($confirmation)
$oldPassword = $env:LOCAL_SSL_PASSWORD
try {
    $plain = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
    $again = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr2)
    if ($plain.Length -lt 8 -or $plain -cne $again) { throw 'Mat khau phai khop va co it nhat 8 ky tu.' }
    $env:LOCAL_SSL_PASSWORD = $plain
    New-Item -ItemType Directory -Path $certDir -Force | Out-Null
    & $keytool -genkeypair -alias local-https -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -validity 365 -storetype PKCS12 -keystore $keyStore -storepass:env LOCAL_SSL_PASSWORD -keypass:env LOCAL_SSL_PASSWORD -dname 'CN=localhost, OU=Khoa CNTT, O=Quan Ly De Tai, C=VN' -ext 'SAN=dns:localhost,ip:127.0.0.1,ip:::1' -ext 'EKU=serverAuth' -ext 'BC=ca:false'
    if ($LASTEXITCODE -ne 0) { throw 'Khong tao duoc chung chi.' }
    & $keytool -exportcert -alias local-https -keystore $keyStore -storepass:env LOCAL_SSL_PASSWORD -file $publicCert
    if ($LASTEXITCODE -ne 0) { throw 'Khong xuat duoc chung chi cong khai.' }
    Write-Host 'DA TAO: .local-https/localhost.p12 va localhost.cer (han 365 ngay).'
    Write-Host 'Chua them vao kho tin cay Windows. Xem docs/HTTPS_LOCAL.md, buoc 2.'
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr2)
    $plain = $null; $again = $null
    $env:LOCAL_SSL_PASSWORD = $oldPassword
}
