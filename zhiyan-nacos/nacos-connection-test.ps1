# Nacos 连接诊断脚本 (Windows PowerShell)
# 用于诊断 Nacos 服务器连接问题

param(
    [string]$ServerAddr = "152.136.245.180",
    [int]$HttpPort = 8848,
    [int]$GrpcPort = 9848
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Nacos 连接诊断工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "目标服务器: $ServerAddr" -ForegroundColor Yellow
Write-Host "HTTP 端口: $HttpPort" -ForegroundColor Yellow
Write-Host "gRPC 端口: $GrpcPort" -ForegroundColor Yellow
Write-Host ""

$allPassed = $true

# 1. 测试 HTTP 端口连接
Write-Host "[1/4] 测试 HTTP 端口 ($HttpPort) 连接..." -ForegroundColor Cyan
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $connect = $tcpClient.BeginConnect($ServerAddr, $HttpPort, $null, $null)
    $wait = $connect.AsyncWaitHandle.WaitOne(5000, $false)
    
    if ($wait) {
        $tcpClient.EndConnect($connect)
        Write-Host "  ✓ HTTP 端口 $HttpPort 连接成功" -ForegroundColor Green
        $tcpClient.Close()
    } else {
        Write-Host "  ✗ HTTP 端口 $HttpPort 连接超时" -ForegroundColor Red
        $allPassed = $false
    }
} catch {
    Write-Host "  ✗ HTTP 端口 $HttpPort 连接失败: $($_.Exception.Message)" -ForegroundColor Red
    $allPassed = $false
}

# 2. 测试 gRPC 端口连接
Write-Host "[2/4] 测试 gRPC 端口 ($GrpcPort) 连接..." -ForegroundColor Cyan
try {
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $connect = $tcpClient.BeginConnect($ServerAddr, $GrpcPort, $null, $null)
    $wait = $connect.AsyncWaitHandle.WaitOne(5000, $false)
    
    if ($wait) {
        $tcpClient.EndConnect($connect)
        Write-Host "  ✓ gRPC 端口 $GrpcPort 连接成功" -ForegroundColor Green
        $tcpClient.Close()
    } else {
        Write-Host "  ✗ gRPC 端口 $GrpcPort 连接超时" -ForegroundColor Red
        Write-Host "    提示: 这是 Nacos 2.x 的关键端口，必须开放！" -ForegroundColor Yellow
        $allPassed = $false
    }
} catch {
    Write-Host "  ✗ gRPC 端口 $GrpcPort 连接失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "    提示: 这是 Nacos 2.x 的关键端口，必须开放！" -ForegroundColor Yellow
    $allPassed = $false
}

# 3. 测试 Nacos 控制台访问
Write-Host "[3/4] 测试 Nacos 控制台访问..." -ForegroundColor Cyan
$consoleUrl = "http://${ServerAddr}:${HttpPort}/nacos"
try {
    $response = Invoke-WebRequest -Uri $consoleUrl -Method Get -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✓ Nacos 控制台可访问: $consoleUrl" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ Nacos 控制台返回状态码: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ✗ Nacos 控制台访问失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "    控制台地址: $consoleUrl" -ForegroundColor Yellow
    $allPassed = $false
}

# 4. 测试 Nacos API 健康检查
Write-Host "[4/4] 测试 Nacos API 健康检查..." -ForegroundColor Cyan
$healthUrl = "http://${ServerAddr}:${HttpPort}/nacos/v1/console/health"
try {
    $response = Invoke-WebRequest -Uri $healthUrl -Method Get -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✓ Nacos API 健康检查通过" -ForegroundColor Green
        Write-Host "    响应: $($response.Content)" -ForegroundColor Gray
    } else {
        Write-Host "  ⚠ Nacos API 返回状态码: $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ✗ Nacos API 健康检查失败: $($_.Exception.Message)" -ForegroundColor Red
    $allPassed = $false
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
if ($allPassed) {
    Write-Host "✓ 所有检查通过！" -ForegroundColor Green
} else {
    Write-Host "✗ 发现问题，请检查以下内容：" -ForegroundColor Red
    Write-Host ""
    Write-Host "1. 确认 Nacos 服务器正在运行" -ForegroundColor Yellow
    Write-Host "2. 确认防火墙已开放端口 $HttpPort 和 $GrpcPort" -ForegroundColor Yellow
    Write-Host "3. 确认网络连接正常（可以 ping $ServerAddr）" -ForegroundColor Yellow
    Write-Host "4. 如果是云服务器，确认安全组规则已配置" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Linux 服务器端检查命令：" -ForegroundColor Cyan
    Write-Host "  netstat -tunlp | grep -E '$HttpPort|$GrpcPort'" -ForegroundColor White
    Write-Host "  firewall-cmd --list-ports" -ForegroundColor White
}
Write-Host "========================================" -ForegroundColor Cyan


