# Лабораторна робота №3: Розгортання застосунку в Kubernetes (Minikube)

Цей каталог містить набір декларативних маніфестів Kubernetes для розгортання мікросервісу `catalog` у локальному кластері **Minikube**.

---

## 1. Склад маніфестів (`k8s/`)

| Файл | Об'єкт Kubernetes | Опис |
| :--- | :--- | :--- |
| `00-namespace.yaml` | `Namespace` | Ізольований простір імен `pipay` для всіх компонентів проєкту. |
| `01-configmap.yaml` | `ConfigMap` | Конфігурація застосунку (привітання, feature flags, профіль). |
| `02-secret.yaml` | `Secret` | Чутливі дані (пароль до БД, секретний API-ключ). |
| `03-pvc.yaml` | `PersistentVolumeClaim` | Запит на виділення сховища 1Gi для збереження даних/стану. |
| `04-deployment.yaml` | `Deployment` | 2 репліки, requests/limits для CPU та пам'яті, liveness/readiness проби, монтування PVC. |
| `05-service.yaml` | `Service` | Сервіс типу `NodePort` (порт 8080 -> nodePort 30080) для доступу з хоста. |
| `06-ingress.yaml` | `Ingress` | Маршрутизація трафіку за доменним ім'ям `catalog.local`. |

---

## 2. Параметри конфігурації

### ConfigMap (`catalog-config`)
* `CATALOG_GREETING`: Текстове повідомлення привітання сервісу.
* `CATALOG_FEATURE_FLAG_NEW_SEARCH`: Прапорець активації нового функціоналу (`true`).
* `INFO_APP_ENVIRONMENT`: Назва середовища розгортання (`k8s-minikube`).
* `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE`: Дозвіл ендпоінтів актуатора (`health,info,metrics,prometheus`).
* `MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED`: Увімкнення ендпоінтів `/actuator/health/liveness` і `/readiness` для проб.
* `MANAGEMENT_HEALTH_RABBIT_ENABLED`: Вимкнення перевірки RabbitMQ у health (брокер у кластері не розгортається).

### Secret (`catalog-secret`)
* `DB_PASSWORD`: Пароль для підключення до бази даних.
* `API_SECRET_KEY`: Секретний токен автентифікації.

---

## 3. Порядок розгортання

### 3.1. Запуск Minikube та доповнень
```bash
# Запуск кластера з драйвером docker
minikube start --driver=docker

# Активація обов'язкових аддонів
minikube addons enable metrics-server
minikube addons enable ingress
```

### 3.2. Завантаження образів у Minikube
```bash
docker build -f catalog/Dockerfile --build-arg APP_VERSION=1.0.0 -t catalog:1.0.0 .
docker build -f catalog/Dockerfile --build-arg APP_VERSION=1.1.0 -t catalog:1.1.0 .
minikube image load catalog:1.0.0
minikube image load catalog:1.1.0
```

### 3.3. Розгортання всіх маніфестів однією командою
```bash
kubectl apply -f k8s/
```

### 3.4. Перевірка стану об'єктів
```bash
kubectl get all -n pipay
kubectl get pvc -n pipay
kubectl get ingress -n pipay
```

### 3.5. Доступ до застосунку
* Через NodePort у браузері:
  ```bash
  minikube service catalog-service -n pipay
  ```
  або за адресою `http://<minikube-ip>:30080/api/v1/catalog/config`
* Перевірка проб актуатора:
  `http://<minikube-ip>:30080/actuator/health`

---

## 4. Демонстрація керування життєвим циклом (для захисту)

### А. Самовідновлення (Self-healing)
Видалення одного з подів — Deployment миттєво створить новий:
```bash
kubectl get pods -n pipay
kubectl delete pod <назва-пода> -n pipay
kubectl get pods -n pipay -w
```

### Б. Масштабування (Scaling)
Збільшення кількості реплік до 4:
```bash
kubectl scale deployment catalog-deployment -n pipay --replicas=4
kubectl get pods -n pipay
```

### В. Оновлення версії (Rolling Update)
Оновлення образу на версію 1.1.0 без зупинки сервісу:
```bash
kubectl set image deployment/catalog-deployment catalog=catalog:1.1.0 -n pipay
kubectl rollout status deployment/catalog-deployment -n pipay
```

### Г. Історія оновлень та відкат (Rollback)
```bash
# Перегляд історії
kubectl rollout history deployment/catalog-deployment -n pipay

# Відкат до попередньої версії
kubectl rollout undo deployment/catalog-deployment -n pipay
kubectl rollout status deployment/catalog-deployment -n pipay
```

---

## 5. Діагностика (Troubleshooting)

### А. Симуляція помилки ImagePullBackOff
```bash
# Встановлення неіснуючого тегу
kubectl set image deployment/catalog-deployment catalog=catalog:non-existent-999 -n pipay

# Перевірка стану (покаже ImagePullBackOff / ErrImagePull)
kubectl get pods -n pipay

# Детальний аналіз подій та опис проблеми
kubectl describe pod <назва-зламаного-пода> -n pipay
```

### Б. Перегляд логів та інтерактивний вхід
```bash
# Перегляд логів контейнера
kubectl logs -n pipay -l app=catalog --tail=50

# Вхід у контейнер для перевірки змінних з ConfigMap і Secret
kubectl exec -it -n pipay deployment/catalog-deployment -- env | grep -E "CATALOG|DB_PASSWORD"
```

### В. Виправлення помилки
```bash
kubectl rollout undo deployment/catalog-deployment -n pipay
```
