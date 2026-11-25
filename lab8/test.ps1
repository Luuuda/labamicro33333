# ЛР8: перевірка API через HTTP-клієнт (curl). Запуск: .\lab8\test.ps1
$DIR  = Split-Path -Parent $MyInvocation.MyCommand.Path
$BASE = (Get-Content (Join-Path $DIR ".api-url")).Trim() + "/restaurants"
function Step($text) { Write-Host "`n=== $text ===" -ForegroundColor Cyan }
Push-Location $DIR

Step "1. POST /restaurants — створити два записи"
$body1 = Join-Path $DIR ".build\r1.json"; New-Item -ItemType Directory -Force (Join-Path $DIR ".build") | Out-Null; '{"name":"Pizza Napoli","cuisine":"italian","rating":4.7}' | Set-Content $body1 -Encoding ascii
$body2 = Join-Path $DIR ".build\r2.json"; '{"name":"Sushi Bar","cuisine":"japanese","rating":4.5}'   | Set-Content $body2 -Encoding ascii
$r1 = curl.exe -s -X POST $BASE -H "Content-Type: application/json" --data-binary "@.build/r1.json"; $r1
$r2 = curl.exe -s -X POST $BASE -H "Content-Type: application/json" --data-binary "@.build/r2.json"; $r2
$id = ($r1 | ConvertFrom-Json).id

Step "2. GET /restaurants — список"
curl.exe -s $BASE

Step "3. GET /restaurants/$id — один запис"
curl.exe -s "$BASE/$id"

Step "4. DELETE /restaurants/$id — видалення"
curl.exe -s -X DELETE "$BASE/$id"

Step "5. GET /restaurants/$id — після видалення (очікуємо 404)"
curl.exe -s -w "`nHTTP %{http_code}`n" "$BASE/$id"

Step "6. POST без name — валідація (очікуємо 400)"
curl.exe -s -w "`nHTTP %{http_code}`n" -X POST $BASE -H "Content-Type: application/json" --data-binary "{}"

Step "7. Дані безпосередньо в DynamoDB"
$env:AWS_ACCESS_KEY_ID="test"; $env:AWS_SECRET_ACCESS_KEY="test"; $env:AWS_DEFAULT_REGION="us-east-1"
aws --endpoint-url http://localhost:4566 dynamodb scan --table-name Restaurants --query "Items[].[id.S,name.S,cuisine.S]" --output table
Pop-Location
