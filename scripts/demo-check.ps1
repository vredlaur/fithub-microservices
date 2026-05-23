param(
    [string]$GatewayUrl = "http://localhost:8080",
    [string]$EurekaUrl = "http://localhost:8761",
    [int]$TimeoutSeconds = 180
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host "[..] $Message" -ForegroundColor Cyan
}

function Write-Ok {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Invoke-FitHubJson {
    param(
        [string]$Method = "GET",
        [string]$Uri,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )

    $parameters = @{
        Method      = $Method
        Uri         = $Uri
        Headers     = $Headers
        ErrorAction = "Stop"
    }

    if ($null -ne $Body) {
        $parameters.ContentType = "application/json"
        $parameters.Body = ($Body | ConvertTo-Json -Depth 8)
    }

    Invoke-RestMethod @parameters
}

function Invoke-WithRetry {
    param(
        [string]$Description,
        [scriptblock]$Action
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastError = $null

    do {
        try {
            return & $Action
        } catch {
            $lastError = $_.Exception.Message
            Start-Sleep -Seconds 5
        }
    } while ((Get-Date) -lt $deadline)

    throw "$Description failed within $TimeoutSeconds seconds. Last error: $lastError"
}

function Wait-ForHealth {
    param(
        [string]$Name,
        [string]$Url
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        try {
            $health = Invoke-FitHubJson -Uri "$Url/actuator/health"
            if ($health.status -eq "UP") {
                Write-Ok "$Name health is UP"
                return
            }
        } catch {
            Start-Sleep -Seconds 3
        }
    } while ((Get-Date) -lt $deadline)

    throw "$Name did not become healthy at $Url/actuator/health within $TimeoutSeconds seconds."
}

function Login-DemoUser {
    param(
        [string]$Username,
        [string]$Password
    )

    $response = Invoke-WithRetry -Description "Login for '$Username' through API Gateway" -Action {
        Invoke-FitHubJson -Method "POST" -Uri "$GatewayUrl/api/auth/login" -Body @{
            username = $Username
            password = $Password
        }
    }

    if ([string]::IsNullOrWhiteSpace($response.token)) {
        throw "Login for '$Username' did not return a JWT token."
    }

    Write-Ok "Login works for '$Username'"
    $response
}

function Get-EurekaApplicationNames {
    try {
        $json = Invoke-FitHubJson -Uri "$EurekaUrl/eureka/apps" -Headers @{ Accept = "application/json" }
        return @($json.applications.application | ForEach-Object { $_.name })
    } catch {
        $xml = Invoke-FitHubJson -Uri "$EurekaUrl/eureka/apps"
        return @($xml.applications.application | ForEach-Object { $_.name })
    }
}

Write-Host "FitHub demo verification" -ForegroundColor Yellow
Write-Host "Gateway: $GatewayUrl"
Write-Host "Eureka:  $EurekaUrl"
Write-Host ""

Write-Step "Checking service health"
Wait-ForHealth -Name "API Gateway" -Url $GatewayUrl
Wait-ForHealth -Name "auth-service" -Url "http://localhost:8081"
Wait-ForHealth -Name "gym-service" -Url "http://localhost:8082"
Wait-ForHealth -Name "booking-service" -Url "http://localhost:8083"

Write-Step "Checking demo logins"
$adminLogin = Login-DemoUser -Username "admin" -Password "Admin123!"
$userLogin = Login-DemoUser -Username "user" -Password "User123!"

$adminHeaders = @{ Authorization = "Bearer $($adminLogin.token)" }
$userHeaders = @{ Authorization = "Bearer $($userLogin.token)" }

Write-Step "Checking Eureka registration"
$expectedApps = @("API-GATEWAY", "AUTH-SERVICE", "GYM-SERVICE", "BOOKING-SERVICE")
$actualApps = @(Get-EurekaApplicationNames)
foreach ($app in $expectedApps) {
    if ($actualApps -notcontains $app) {
        throw "Eureka does not contain $app. Actual apps: $($actualApps -join ', ')"
    }
}
Write-Ok "Eureka contains expected applications: $($expectedApps -join ', ')"

Write-Step "Checking current user/client mapping"
$me = Invoke-WithRetry -Description "Reading /api/auth/me" -Action {
    Invoke-FitHubJson -Uri "$GatewayUrl/api/auth/me" -Headers $userHeaders
}
if ($me.username -ne "user") {
    throw "Expected /api/auth/me to return username 'user', received '$($me.username)'."
}

$client = Invoke-WithRetry -Description "Reading /api/clients/me" -Action {
    Invoke-FitHubJson -Uri "$GatewayUrl/api/clients/me" -Headers $userHeaders
}
if ($null -eq $client.id) {
    throw "/api/clients/me did not return a client id."
}
Write-Ok "/api/clients/me returned client #$($client.id) for auth user #$($client.authUserId)"

Write-Step "Checking class listing and availability"
$classes = Invoke-WithRetry -Description "Reading /api/classes through API Gateway" -Action {
    Invoke-FitHubJson -Uri "$GatewayUrl/api/classes?page=0&size=1&sort=startTime,asc" -Headers $adminHeaders
}
$class = @($classes.content)[0]
if ($null -eq $class -or $null -eq $class.id) {
    throw "No fitness class found. Reset the demo database or create a class as admin."
}

$availability = Invoke-WithRetry -Description "Reading class availability through API Gateway" -Action {
    Invoke-FitHubJson -Uri "$GatewayUrl/api/classes/$($class.id)/availability" -Headers $adminHeaders
}
Write-Ok "Found class #$($class.id) '$($class.name)' with $($availability.availableSlots) available slots"

Write-Step "Checking booking flow through API Gateway"
$bookings = Invoke-WithRetry -Description "Reading existing bookings through API Gateway" -Action {
    Invoke-FitHubJson -Uri "$GatewayUrl/api/bookings/me?page=0&size=100&sort=id,desc" -Headers $userHeaders
}
$existingBooking = @($bookings.content | Where-Object {
    $_.client.id -eq $client.id -and $_.fitnessClassId -eq $class.id -and $_.status -eq "CONFIRMED"
})[0]

if ($null -ne $existingBooking) {
    Write-Ok "Confirmed demo booking #$($existingBooking.id) already exists for class #$($class.id)"
} elseif ($availability.available -eq $true) {
    $booking = Invoke-WithRetry -Description "Creating booking through API Gateway" -Action {
        Invoke-FitHubJson -Method "POST" -Uri "$GatewayUrl/api/bookings/me" -Headers $userHeaders -Body @{
            fitnessClassId = $class.id
        }
    }

    if ($booking.status -ne "CONFIRMED") {
        throw "Booking was created but status is '$($booking.status)', expected CONFIRMED."
    }

    Write-Ok "Created booking #$($booking.id) for class #$($class.id)"
} else {
    throw "Class #$($class.id) has no available slots and no existing confirmed demo booking was found. Run .\scripts\demo-reset.ps1 for a clean demo."
}

Write-Host ""
Write-Host "Demo check completed successfully." -ForegroundColor Green
