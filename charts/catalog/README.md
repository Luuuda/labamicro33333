# Helm-чарт `catalog` (Лабораторна робота №4)

Чарт пакує мікросервіс **catalog** проєкту Pipay і створює той самий набір об'єктів, що й маніфести з `k8s/` (ЛР3):
ConfigMap, Secret, PersistentVolumeClaim, Deployment, Service, Ingress. Namespace створюється самим Helm
(`--create-namespace`), щоб `helm uninstall` не видаляв namespace разом із даними Helm.

## Структура

```
helm/catalog/
├── Chart.yaml            # метадані чарта: name, description, version, appVersion
├── values.yaml           # значення за замовчуванням (= dev)
├── values-dev.yaml       # середовище розробки
├── values-prod.yaml      # продакшн-середовище
└── templates/
    ├── _helpers.tpl      # іменовані шаблони: fullname, labels, selectorLabels, imageTag
    ├── configmap.yaml
    ├── secret.yaml
    ├── pvc.yaml          # тільки якщо persistence.enabled=true
    ├── deployment.yaml
    ├── service.yaml
    ├── ingress.yaml      # тільки якщо ingress.enabled=true
    ├── pre-upgrade-job.yaml   # хук pre-upgrade
    ├── tests/test-connection.yaml  # тест для helm test
    └── NOTES.txt         # підказка після встановлення
```

## version і appVersion

| Поле | Значення | Що означає |
| :--- | :--- | :--- |
| `version` | `0.1.0` | Версія **самого чарта** (шаблонів і values). Змінюється, коли змінюється чарт, навіть якщо застосунок той самий. |
| `appVersion` | `"1.0.0"` | Версія **застосунку**, тобто образу `catalog`. Використовується як тег образу, якщо `image.tag` порожній. |

Наприклад, якщо додати в чарт новий шаблон — піднімаємо `version` до `0.2.0`, а `appVersion` лишається `1.0.0`.
Якщо вийшла нова версія застосунку — змінюється `appVersion` (або `image.tag`), а `version` чарта піднімається на patch.

## Параметри values

| Параметр | За замовчуванням (dev) | prod | Опис |
| :--- | :--- | :--- | :--- |
| `replicaCount` | `1` | `3` | Кількість реплік Deployment |
| `image.repository` | `catalog` | — | Репозиторій образу |
| `image.tag` | `"1.0.0"` | `"1.1.0"` | Тег образу (порожній → `appVersion`) |
| `image.pullPolicy` | `IfNotPresent` | — | Політика завантаження образу |
| `strategy.maxSurge` / `maxUnavailable` | `1` / `0` | — | Параметри rolling update |
| `service.type` | `NodePort` | — | Тип сервісу |
| `service.port` | `8080` | — | Порт сервісу |
| `service.nodePort` | `""` (авто) | — | Фіксований nodePort (порожній — призначається автоматично) |
| `resources` | 100m/256Mi – 500m/512Mi | 200m/384Mi – 1000m/768Mi | requests / limits CPU і пам'яті |
| `probes.liveness.*`, `probes.readiness.*` | `/actuator/health/...` | — | Параметри проб |
| `config.greeting` | `Привіт з Helm-чарта (dev)!` | `...(prod)!` | `CATALOG_GREETING` |
| `config.featureFlagNewSearch` | `"false"` | `"true"` | `CATALOG_FEATURE_FLAG_NEW_SEARCH` |
| `config.environment` | `dev` | `prod` | `INFO_APP_ENVIRONMENT` |
| `config.actuatorExposure` | `health,info,metrics,prometheus` | — | `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` |
| `config.healthProbesEnabled` | `"true"` | — | `MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED` |
| `config.rabbitHealthEnabled` | `"false"` | — | `MANAGEMENT_HEALTH_RABBIT_ENABLED` |
| `secret.dbPassword` | `dev-password` | `CHANGE_ME` (через `--set`) | `DB_PASSWORD` у Secret |
| `secret.apiSecretKey` | `dev-api-key` | `CHANGE_ME` (через `--set`) | `API_SECRET_KEY` у Secret |
| `ingress.enabled` | `false` | `true` | Чи створювати Ingress |
| `ingress.className` / `host` / `path` | `nginx` / `catalog.local` / `/` | — | Параметри Ingress |
| `persistence.enabled` | `true` | — | Чи створювати PVC |
| `persistence.size` | `1Gi` | — | Розмір сховища (не змінюється між середовищами: StorageClass `standard` у minikube не підтримує розширення PVC) |
| `persistence.storageClass` | `""` | — | StorageClass (порожній — за замовчуванням) |
| `hooks.preUpgrade.enabled` | `true` | — | Хук pre-upgrade |
| `tests.enabled` | `true` | — | Тест для `helm test` |

## Встановлення

Попередньо: образи `catalog:1.0.0` і `catalog:1.1.0` завантажені в minikube (`minikube image load ...`).

```bash
# перевірка
helm lint helm/catalog

# dev
helm install pipay helm/catalog -n pipay --create-namespace -f helm/catalog/values-dev.yaml

# prod (паролі передаються через --set, у файлі їх немає)
helm install pipay helm/catalog -n pipay --create-namespace -f helm/catalog/values-prod.yaml \
  --set secret.dbPassword=<пароль> --set secret.apiSecretKey=<ключ>

# тест релізу
helm test pipay -n pipay

# видалення
helm uninstall pipay -n pipay
```

## Пріоритет значень

Від найнижчого до найвищого:

1. `values.yaml` у чарті;
2. файли `-f` / `--values` (якщо їх кілька — пізніший перекриває попередній);
3. прапорці `--set` (найвищий пріоритет).

Наприклад, `-f values-prod.yaml --set replicaCount=2` дасть 2 репліки, хоча в `values-prod.yaml` вказано 3.

## Де Helm зберігає стан релізу

Кожна ревізія релізу зберігається в Secret типу `helm.sh/release.v1` у namespace релізу
(ім'я `sh.helm.release.v1.<реліз>.v<ревізія>`):

```bash
kubectl get secrets -n pipay -l owner=helm
```
