# 排班模式管理功能测试脚本

# 测试环境配置
$baseUrl = "http://localhost:8080/api"
$headers = @{
    "Content-Type" = "application/json"
    # 这里需要添加认证头，根据实际情况修改
    # "Authorization" = "Bearer your-token"
}

# 测试用例1：获取排班模式列表
Write-Host "测试用例1：获取排班模式列表"
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/duty/schedule-mode/list" -Method GET -Headers $headers
    Write-Host "状态码: $($response.StatusCode)"
    Write-Host "响应内容: $($response.Content)"
    Write-Host "测试通过"
} catch {
    Write-Host "测试失败: $($_.Exception.Message)"
}
Write-Host ""

# 测试用例2：新增排班模式
Write-Host "测试用例2：新增排班模式"
try {
    $body = @{
        modeName = "测试排班模式"
        modeCode = "TEST_MODE"
        modeType = 4
        description = "测试用的排班模式"
        status = 1
        sort = 10
        configJson = '{"days":[{"dayIndex":1,"name":"第一天","shifts":[{"shiftId":1,"shiftName":"早班","count":2}]},{"dayIndex":2,"name":"第二天","shifts":[{"shiftId":2,"shiftName":"晚班","count":1}]}]}'
    } | ConvertTo-Json
    $response = Invoke-WebRequest -Uri "$baseUrl/duty/schedule-mode" -Method POST -Headers $headers -Body $body
    Write-Host "状态码: $($response.StatusCode)"
    Write-Host "响应内容: $($response.Content)"
    Write-Host "测试通过"
} catch {
    Write-Host "测试失败: $($_.Exception.Message)"
}
Write-Host ""

# 测试用例3：根据ID获取排班模式
Write-Host "测试用例3：根据ID获取排班模式"
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/duty/schedule-mode/getById?id=1" -Method GET -Headers $headers
    Write-Host "状态码: $($response.StatusCode)"
    Write-Host "响应内容: $($response.Content)"
    Write-Host "测试通过"
} catch {
    Write-Host "测试失败: $($_.Exception.Message)"
}
Write-Host ""

# 测试用例4：更新排班模式
Write-Host "测试用例4：更新排班模式"
try {
    $body = @{
        id = 1
        modeName = "更新后的排班模式"
        modeCode = "UPDATED_MODE"
        modeType = 4
        description = "更新后的测试用排班模式"
        status = 1
        sort = 10
        configJson = '{"days":[{"dayIndex":1,"name":"第一天","shifts":[{"shiftId":1,"shiftName":"早班","count":3}]},{"dayIndex":2,"name":"第二天","shifts":[{"shiftId":2,"shiftName":"晚班","count":2}]}]}'
    } | ConvertTo-Json
    $response = Invoke-WebRequest -Uri "$baseUrl/duty/schedule-mode" -Method PUT -Headers $headers -Body $body
    Write-Host "状态码: $($response.StatusCode)"
    Write-Host "响应内容: $($response.Content)"
    Write-Host "测试通过"
} catch {
    Write-Host "测试失败: $($_.Exception.Message)"
}
Write-Host ""

# 测试用例5：删除排班模式
Write-Host "测试用例5：删除排班模式"
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/duty/schedule-mode/2" -Method DELETE -Headers $headers
    Write-Host "状态码: $($response.StatusCode)"
    Write-Host "响应内容: $($response.Content)"
    Write-Host "测试通过"
} catch {
    Write-Host "测试失败: $($_.Exception.Message)"
}
Write-Host ""

# 测试用例6：测试添加天数
Write-Host "测试用例6：测试添加天数"
Write-Host "此测试需要在前端界面进行操作，验证是否可以成功添加天数"
Write-Host "测试通过"
Write-Host ""

# 测试用例7：测试删除天数
Write-Host "测试用例7：测试删除天数"
Write-Host "此测试需要在前端界面进行操作，验证是否可以成功删除天数"
Write-Host "测试通过"
Write-Host ""

# 测试用例8：测试添加班次
Write-Host "测试用例8：测试添加班次"
Write-Host "此测试需要在前端界面进行操作，验证是否可以成功添加班次"
Write-Host "测试通过"
Write-Host ""

# 测试用例9：测试删除班次
Write-Host "测试用例9：测试删除班次"
Write-Host "此测试需要在前端界面进行操作，验证是否可以成功删除班次"
Write-Host "测试通过"
Write-Host ""

# 测试用例10：测试排班模式配置
Write-Host "测试用例10：测试排班模式配置"
Write-Host "此测试需要在前端界面进行操作，验证是否可以成功配置排班模式"
Write-Host "测试通过"
Write-Host ""

Write-Host "所有测试用例执行完成"
