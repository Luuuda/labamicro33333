# ЛР8: автоматичне розгортання serverless-застосунку в LocalStack через AWS CLI.
# Запуск з кореня проєкту:  .\lab8\deploy.ps1
# Скрипт ідемпотентний: перед створенням видаляє ресурси з попереднього запуску.

$ErrorActionPreference = "Stop"
$env:AWS_ACCESS_KEY_ID     = "test"     # LocalStack приймає будь-які ключі
$env:AWS_SECRET_ACCESS_KEY = "test"
$env:AWS_DEFAULT_REGION    = "us-east-1"
$EP       = "http://localhost:4566"
$REGION   = "us-east-1"
$ACCOUNT  = "000000000000"
$TABLE    = "Restaurants"
$FUNC     = "catalog-api"
$API_NAME = "catalog-api"
$STAGE    = "dev"
$DIR      = Split-Path -Parent $MyInvocation.MyCommand.Path

# виклик AWS CLI проти LocalStack; у режимі Stop зупиняє скрипт при помилці
function aws-local {
  aws --endpoint-url $EP @args
  if ($LASTEXITCODE -ne 0 -and $ErrorActionPreference -eq "Stop") { throw "AWS CLI error (код $LASTEXITCODE)" }
}
function Step($text) { Write-Host "`n=== $text ===" -ForegroundColor Cyan }

# робоча папка для тимчасових файлів (zip, json); шляхи відносні —
# так AWS CLI не має проблем з кирилицею в шляху до профілю користувача
Push-Location $DIR
New-Item -ItemType Directory -Force -Path ".build" | Out-Null

Step "0. Очищення попереднього розгортання"
$ErrorActionPreference = "Continue"
$old = aws-local apigateway get-rest-apis --query "items[?name=='$API_NAME'].id" --output text
foreach ($id in ($old -split "\s+" | Where-Object { $_ })) { aws-local apigateway delete-rest-api --rest-api-id $id | Out-Null }
aws-local lambda delete-function --function-name $FUNC 2>$null | Out-Null
aws-local dynamodb delete-table --table-name $TABLE 2>$null | Out-Null
aws-local iam delete-role --role-name lambda-role 2>$null | Out-Null
$ErrorActionPreference = "Stop"

Step "1. Таблиця DynamoDB '$TABLE' (ключ id)"
aws-local dynamodb create-table --table-name $TABLE `
  --attribute-definitions "AttributeName=id,AttributeType=S" `
  --key-schema "AttributeName=id,KeyType=HASH" `
  --billing-mode PAY_PER_REQUEST --query "TableDescription.[TableName,TableStatus]" --output text
aws-local dynamodb wait table-exists --table-name $TABLE

Step "2. IAM-роль для Lambda"
$trust = '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"lambda.amazonaws.com"},"Action":"sts:AssumeRole"}]}'
$trustFile = ".build/lambda-trust.json"
Set-Content -Path $trustFile -Value $trust -Encoding ascii
$ROLE_ARN = aws-local iam create-role --role-name lambda-role --assume-role-policy-document "file://$trustFile" --query "Role.Arn" --output text
Write-Host $ROLE_ARN

Step "3. Lambda-функція '$FUNC' (Python 3.12)"
$zip = ".build/catalog-api.zip"
Compress-Archive -Path "lambda/handler.py" -DestinationPath $zip -Force
$FUNC_ARN = aws-local lambda create-function --function-name $FUNC `
  --runtime python3.12 --handler handler.lambda_handler --role $ROLE_ARN `
  --zip-file "fileb://$zip" --timeout 15 `
  --environment "Variables={TABLE_NAME=$TABLE}" --query "FunctionArn" --output text
Write-Host $FUNC_ARN
aws-local lambda wait function-active-v2 --function-name $FUNC

Step "4. API Gateway: REST API, ресурси /restaurants і /restaurants/{id}"
$API_ID  = aws-local apigateway create-rest-api --name $API_NAME --query "id" --output text
$ROOT_ID = aws-local apigateway get-resources --rest-api-id $API_ID --query "items[?path=='/'].id" --output text
$RES_ID  = aws-local apigateway create-resource --rest-api-id $API_ID --parent-id $ROOT_ID --path-part restaurants --query "id" --output text
$ITEM_ID = aws-local apigateway create-resource --rest-api-id $API_ID --parent-id $RES_ID --path-part "{id}" --query "id" --output text
$URI = "arn:aws:apigateway:${REGION}:lambda:path/2015-03-31/functions/$FUNC_ARN/invocations"
foreach ($r in @($RES_ID, $ITEM_ID)) {
  aws-local apigateway put-method --rest-api-id $API_ID --resource-id $r --http-method ANY --authorization-type NONE | Out-Null
  aws-local apigateway put-integration --rest-api-id $API_ID --resource-id $r --http-method ANY `
    --type AWS_PROXY --integration-http-method POST --uri $URI | Out-Null
}
Write-Host "API id: $API_ID"

Step "5. Дозвіл API Gateway викликати Lambda і розгортання стадії '$STAGE'"
aws-local lambda add-permission --function-name $FUNC --statement-id apigw `
  --action lambda:InvokeFunction --principal apigateway.amazonaws.com `
  --source-arn "arn:aws:execute-api:${REGION}:${ACCOUNT}:$API_ID/*/*/*" | Out-Null
aws-local apigateway create-deployment --rest-api-id $API_ID --stage-name $STAGE --query "id" --output text

$BASE = "$EP/restapis/$API_ID/$STAGE/_user_request_"
Set-Content -Path ".api-url" -Value $BASE -Encoding ascii
Step "Готово"
Write-Host "Базова адреса API: $BASE/restaurants" -ForegroundColor Green
Pop-Location
