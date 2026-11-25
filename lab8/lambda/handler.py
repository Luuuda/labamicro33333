"""
ЛР8. Lambda-функція catalog-api: HTTP API ресторанів поверх DynamoDB.

Маршрути (API Gateway, проксі-інтеграція):
  POST   /restaurants        — створити запис
  GET    /restaurants        — список усіх записів
  GET    /restaurants/{id}   — отримати запис
  DELETE /restaurants/{id}   — видалити запис
"""
import json
import os
import uuid
from datetime import datetime, timezone
from decimal import Decimal

import boto3

TABLE_NAME = os.environ.get("TABLE_NAME", "Restaurants")
# у LocalStack Lambda отримує адресу емулятора через AWS_ENDPOINT_URL
ENDPOINT = os.environ.get("AWS_ENDPOINT_URL")

dynamodb = boto3.resource("dynamodb", endpoint_url=ENDPOINT) if ENDPOINT else boto3.resource("dynamodb")
table = dynamodb.Table(TABLE_NAME)


def _json(value):
    """DynamoDB повертає числа як Decimal — перетворюємо для JSON."""
    if isinstance(value, Decimal):
        return int(value) if value % 1 == 0 else float(value)
    raise TypeError


def response(status, body):
    return {
        "statusCode": status,
        "headers": {"Content-Type": "application/json"},
        "body": json.dumps(body, default=_json, ensure_ascii=False),
    }


def create(event):
    try:
        data = json.loads(event.get("body") or "{}")
    except json.JSONDecodeError:
        return response(400, {"error": "body must be valid JSON"})
    name = (data.get("name") or "").strip()
    if not name:
        return response(400, {"error": "field 'name' is required"})
    item = {
        "id": str(uuid.uuid4()),
        "name": name,
        "cuisine": data.get("cuisine", "unknown"),
        "rating": Decimal(str(data.get("rating", 0))),
        "createdAt": datetime.now(timezone.utc).isoformat(),
    }
    table.put_item(Item=item)
    return response(201, item)


def list_all(_event):
    items = table.scan().get("Items", [])
    return response(200, {"count": len(items), "items": items})


def get_one(item_id):
    item = table.get_item(Key={"id": item_id}).get("Item")
    if not item:
        return response(404, {"error": f"restaurant {item_id} not found"})
    return response(200, item)


def delete_one(item_id):
    result = table.delete_item(Key={"id": item_id}, ReturnValues="ALL_OLD")
    if "Attributes" not in result:
        return response(404, {"error": f"restaurant {item_id} not found"})
    return response(200, {"deleted": item_id})


def lambda_handler(event, context):
    method = event.get("httpMethod")
    item_id = (event.get("pathParameters") or {}).get("id")
    print(json.dumps({"method": method, "path": event.get("path"), "id": item_id}))

    if item_id is None:
        if method == "POST":
            return create(event)
        if method == "GET":
            return list_all(event)
    else:
        if method == "GET":
            return get_one(item_id)
        if method == "DELETE":
            return delete_one(item_id)
    return response(405, {"error": f"method {method} not allowed"})
