# Pipay Microservices 

Цей проект є мікросервісним додатком на базі Java 22 та Spring Boot 3.2.3.
Проект містить наступні мікросервіси:
- **catalog**
- **users**
- **reviews**
- **gateway**
- **config-server**

## Запуск проекту через Docker
Для повноцінного запуску всього середовища використовується Docker Compose.

1. Переконайтесь, що Docker та Docker Compose встановлені.
2. Виконайте команду в кореневій папці проекту:
```bash
docker-compose up --build -d
```
Ця команда автоматично скомпілює вихідний код через Maven всередині контейнерів і запустить усі необхідні мікросервіси.

## Архітектура та технології
- **Мова:** Java 22
- **Фреймворк:** Spring Boot 3.2.3, Spring Cloud
- **Взаємодія з БД:** Spring Data JPA
- **Моніторинг:** Prometheus, Grafana
- **Тестування:** JUnit, Spring Boot Test

## Розгортання в Kubernetes
- **ЛР3 — маніфести:** каталог [`k8s/`](k8s/README.md), розгортання `kubectl apply -f k8s/`.
- **ЛР4 — Helm-чарт:** каталог [`helm/catalog/`](helm/catalog/README.md), розгортання
  `helm install pipay helm/catalog -n pipay --create-namespace -f helm/catalog/values-dev.yaml`.
- **ЛР5 — GitOps (Argo CD):** чарт `charts/catalog/` зі значеннями `values-minikube.yaml`,
  Application `argocd-apps/catalog.yaml`, кореневий Application `bootstrap/root.yaml`
  (єдиний ручний крок: `kubectl apply -f bootstrap/root.yaml`). Secret `catalog-secret`
  створюється вручну в namespace `pipay` і в Git не зберігається.
