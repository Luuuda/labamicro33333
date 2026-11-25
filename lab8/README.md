# ЛР8 — Serverless-застосунок у LocalStack

HTTP API каталогу ресторанів: **API Gateway → Lambda → DynamoDB**, розгорнуте в локальному емуляторі AWS (LocalStack).

## Архітектура

```
 HTTP-клієнт (curl)
        │  POST/GET/DELETE /restaurants[/{id}]
        ▼
 ┌─────────────────────┐   AWS_PROXY    ┌──────────────────────┐   boto3    ┌──────────────────────┐
 │ API Gateway (REST)  │ ─────────────▶ │ Lambda  catalog-api  │ ─────────▶ │ DynamoDB Restaurants │
 │ стадія dev          │ ◀───────────── │ Python 3.12          │ ◀───────── │ ключ id (String)     │
 └─────────────────────┘   JSON-відповідь└──────────────────────┘            └──────────────────────┘
              усе працює в одному контейнері LocalStack (порт 4566)
```

| Компонент | Ресурс | Призначення |
| :--- | :--- | :--- |
| API Gateway | REST API `catalog-api`, ресурси `/restaurants`, `/restaurants/{id}`, метод `ANY` | приймає HTTP-запити й передає їх у Lambda (проксі-інтеграція) |
| Lambda | `catalog-api`, `handler.lambda_handler` | маршрутизує за методом і шляхом, перевіряє дані, працює з таблицею |
| DynamoDB | таблиця `Restaurants`, ключ `id`, оплата за запити | зберігає записи |
| IAM | роль `lambda-role` | роль виконання Lambda (у LocalStack — формальна) |

## API

| Метод | Шлях | Опис | Відповідь |
| :--- | :--- | :--- | :--- |
| POST | `/restaurants` | створити запис `{"name","cuisine","rating"}` | 201 + запис з `id` / 400 без `name` |
| GET | `/restaurants` | усі записи | 200 `{"count","items"}` |
| GET | `/restaurants/{id}` | один запис | 200 / 404 |
| DELETE | `/restaurants/{id}` | видалити | 200 / 404 |

## Послідовність розгортання

1. Запустити LocalStack: `docker compose -f lab8/docker-compose.yml up -d` (потрібен `LOCALSTACK_AUTH_TOKEN`).
2. `.\lab8\deploy.ps1` — створює таблицю → роль → Lambda → API Gateway → дозвіл → стадію `dev` і друкує адресу API.
3. `.\lab8\test.ps1` — перевіряє створення, отримання, список, видалення, 404 і 400.

Адреса API в LocalStack: `http://localhost:4566/restapis/<api-id>/dev/_user_request_/restaurants`.
