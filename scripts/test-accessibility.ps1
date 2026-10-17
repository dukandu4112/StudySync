# Run with Windows PowerShell, whose desktop framework provides UI Automation.
$ErrorActionPreference = "Stop"
Add-Type -AssemblyName UIAutomationClient
Add-Type -AssemblyName UIAutomationTypes
$root = [System.Windows.Automation.AutomationElement]::RootElement
$window = $null
for ($attempt = 0; $attempt -lt 40; $attempt++) {
    $children = $root.FindAll([System.Windows.Automation.TreeScope]::Children, [System.Windows.Automation.Condition]::TrueCondition)
    foreach ($child in $children) {
        if ($child.Current.Name -like "StudySync 3.0*") { $window = $child; break }
    }
    if ($window) { break }
    Start-Sleep -Milliseconds 250
}
if (-not $window) { throw "StudySync window is missing from the Windows accessibility tree" }
$checks = @(
    @{ Name = "Daily study goal (minutes)"; Type = [System.Windows.Automation.ControlType]::Edit },
    @{ Name = "Create database backup"; Type = [System.Windows.Automation.ControlType]::Button },
    @{ Name = "Restore database backup"; Type = [System.Windows.Automation.ControlType]::Button },
    @{ Name = "Export CSV report"; Type = [System.Windows.Automation.ControlType]::Button }
)
foreach ($check in $checks) {
    # Windows uses the associated visible label as an edit control's name.
    # Match the role too, since JavaFX also exposes the label's text node.
    $nameCondition = [System.Windows.Automation.PropertyCondition]::new([System.Windows.Automation.AutomationElement]::NameProperty, $check.Name)
    $typeCondition = [System.Windows.Automation.PropertyCondition]::new([System.Windows.Automation.AutomationElement]::ControlTypeProperty, $check.Type)
    $condition = [System.Windows.Automation.AndCondition]::new($nameCondition, $typeCondition)
    $control = $null
    for ($attempt = 0; $attempt -lt 40; $attempt++) {
        $control = $window.FindFirst([System.Windows.Automation.TreeScope]::Descendants, $condition)
        if ($control) { break }
        Start-Sleep -Milliseconds 250
    }
    if (-not $control) {
        $nodes = $window.FindAll([System.Windows.Automation.TreeScope]::Descendants, [System.Windows.Automation.Condition]::TrueCondition)
        foreach ($node in $nodes) { Write-Host "ACCESSIBILITY NODE: $($node.Current.Name) | $($node.Current.ControlType.ProgrammaticName)" }
        throw "Missing accessible control: $($check.Name)"
    }
    if ($control.Current.ControlType -ne $check.Type) { throw "Incorrect accessible role: $($check.Name)" }
    if (-not $control.Current.IsKeyboardFocusable) { throw "Control is not keyboard focusable: $($check.Name)" }
    Write-Host "PASS accessibility: $($check.Name), $($control.Current.ControlType.ProgrammaticName), keyboard focusable"
}
