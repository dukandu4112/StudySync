$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
Add-Type -AssemblyName System.Windows.Forms
$windows = New-Object -ComObject WScript.Shell
$qaRoot = Join-Path $env:RUNNER_TEMP ("studysync-native-" + [guid]::NewGuid())
New-Item -ItemType Directory -Force $qaRoot | Out-Null
$database = Join-Path $qaRoot "live.db"
$backup = Join-Path $qaRoot "backup.db"
$report = Join-Path $qaRoot "report.csv"
$jar = "target/studysync-3.1.0-all.jar"
function Fixture([string]$mode) {
    & java -cp $jar scripts/NativeDataFixture.java $mode $database
    if ($LASTEXITCODE -ne 0) { throw "Fixture validation failed: $mode" }
}
function Wait-Window([string]$title) {
    for ($attempt = 0; $attempt -lt 40; $attempt++) {
        if ($windows.AppActivate($title)) { Start-Sleep -Milliseconds 400; return }
        Start-Sleep -Milliseconds 250
    }
    throw "Window did not open: $title"
}
function Send([string]$keys) { [System.Windows.Forms.SendKeys]::SendWait($keys); Start-Sleep -Milliseconds 300 }
function Select-Path([string]$title, [string]$path) {
    Wait-Window $title
    Send "%n"
    Send "^a"
    Send $path
    Send "{ENTER}"
}
function Wait-File([string]$path) {
    for ($attempt = 0; $attempt -lt 40; $attempt++) {
        if (Test-Path $path) { return }
        Start-Sleep -Milliseconds 250
    }
    throw "Expected file was not created: $path"
}
$priorOptions = $env:JAVA_TOOL_OPTIONS
$app = $null
try {
    Fixture "seed"
    $env:JAVA_TOOL_OPTIONS = "-Duser.home=$qaRoot -Dstudysync.database=jdbc:sqlite:$database -Djavafx.accessibility.force=true"
    $app = Start-Process target/desktop/StudySync/StudySync.exe -PassThru -RedirectStandardOutput (Join-Path $qaRoot "app.stdout.log") -RedirectStandardError (Join-Path $qaRoot "app.stderr.log")
    $env:JAVA_TOOL_OPTIONS = $priorOptions
    Write-Host "Interactive desktop: $([Environment]::UserInteractive)"
    Wait-Window "StudySync 3.1"
    Send "^9"
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/test-accessibility.ps1
    if ($LASTEXITCODE -ne 0) { throw "Windows accessibility metadata checks failed" }
    Send "%b"
    Select-Path "Create backup" $backup
    Wait-File $backup
    Write-Host "PASS: real Save dialog created database backup"
    Wait-Window "StudySync 3.1"
    Send "%b"
    Wait-Window "Create backup"
    Send "{ESC}"
    Write-Host "PASS: Save dialog cancellation"
    Wait-Window "StudySync 3.1"
    Send "%e"
    Select-Path "Export report" $report
    Wait-File $report
    if (-not (Get-Content $report -Raw).Contains("NATIVE101")) { throw "CSV lacks fixture data" }
    Write-Host "PASS: real Save dialog exported CSV"
    Fixture "mutate"
    Wait-Window "StudySync 3.1"
    Send "%r"
    Wait-Window "Choose StudySync backup"
    Send "{ESC}"
    Fixture "mutated"
    Write-Host "PASS: Open dialog cancellation preserves data"
    Wait-Window "StudySync 3.1"
    Send "%r"
    Select-Path "Choose StudySync backup" $backup
    Wait-Window "Confirmation"
    Send "{ESC}"
    Fixture "mutated"
    Write-Host "PASS: restore confirmation cancellation preserves data"
    Wait-Window "StudySync 3.1"
    Send "%r"
    Select-Path "Choose StudySync backup" $backup
    Wait-Window "Confirmation"
    Send "{ENTER}"
    Start-Sleep -Milliseconds 1000
    Fixture "original"
    $snapshots = @(Get-ChildItem (Join-Path $qaRoot ".studysync/recovery-backups") -Filter "*.db")
    if ($snapshots.Count -ne 1) { throw "Expected one recovery snapshot" }
    Write-Host "PASS: real Open dialog restored data and preserved recovery snapshot"
    Stop-Process -Id $app.Id
    $app = Start-Process target/desktop/StudySync/StudySync.exe -PassThru -Environment @{JAVA_TOOL_OPTIONS="-Duser.home=$qaRoot -Dstudysync.database=jdbc:sqlite:$database"}
    Wait-Window "StudySync 3.1"
    Fixture "original"
    Write-Host "PASS: packaged application reopens with restored data"
} catch {
    Get-Process | Where-Object { $_.ProcessName -match 'StudySync|java' } | Select-Object Id,ProcessName,MainWindowHandle,MainWindowTitle | Format-Table -AutoSize
    Get-Content (Join-Path $qaRoot "app.stdout.log") -ErrorAction SilentlyContinue
    Get-Content (Join-Path $qaRoot "app.stderr.log") -ErrorAction SilentlyContinue
    throw
} finally {
    $env:JAVA_TOOL_OPTIONS = $priorOptions
    if ($app -and -not $app.HasExited) { Stop-Process -Id $app.Id -ErrorAction SilentlyContinue }
}
