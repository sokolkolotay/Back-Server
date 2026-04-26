# 🖥️ Notes API — Ktor Backend

REST API для управления заметками. Pet-проект с полным DevOps стеком: от кода до мониторинга на реальном VPS.

## 🏗️ Архитектура
Internet → Nginx :80 → Ktor API :8080 → PostgreSQL
↓
Prometheus → Grafana
GitHub push → Actions → Docker Hub → VPS (автодеплой)

## ⚙️ Технологии

| Слой | Технология |
|---|---|
| Backend | Kotlin, Ktor 3, Exposed ORM |
| База данных | PostgreSQL 15 |
| Контейнеризация | Docker, Docker Compose |
| Reverse Proxy | Nginx |
| CI/CD | GitHub Actions |
| Мониторинг | Prometheus + Grafana |
| Логирование | Loki + Promtail |

## 📡 Эндпоинты

| Метод | Путь | Описание |
|---|---|---|
| GET | /health | Статус сервера |
| GET | /notes | Все заметки |
| GET | /notes/{id} | Заметка по ID |
| POST | /notes | Создать заметку |
| PUT | /notes/{id} | Обновить заметку |
| DELETE | /notes/{id} | Удалить заметку |

## 🚀 Запуск локально

```bash
git clone https://github.com/sokolkolotay/Back-Server.git
cd Back-Server
docker compose up --build
```

API будет доступен на `http://localhost/health`

## 📊 Мониторинг

- Grafana: `http://185.73.124.70:3000`
- Prometheus: `http://185.73.124.70:9090`

Дашборд включает метрики JVM (heap, CPU), количество запросов и время ответа.

## 🔄 CI/CD Pipeline

При каждом пуше в `main`:
1. GitHub Actions собирает Docker образ
2. Пушит на Docker Hub
3. Заходит на VPS по SSH
4. Перезапускает контейнеры с новой версией
