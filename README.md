Веб-приложение онлайн-магазина с фронтом на шаблонизаторе Thymeleaf.
Задание по курсу Яндекс.Практикум "Middle Java разработчик"

Сборка проекта:
```bash
mvn clean package
```

Запуск:
- Без контейнера
```bash
java -jar target/my-market-app-0.0.1.jar --server.port=8080
```
- В контейнере
Шаг 1. Собрать образ
```bash
docker build -t mymarket_image .  
```
Шаг 2. Запустить контейнер из образа (CATALOG_CSV - опциональный параметр с путём до CSV файла с каталогом)
```bash
docker run --name mymarket_container --add-host='host.docker.internal:host-gateway' -p 8080:8080 --env-file env/.env mymarket_image:latest        
```
Вместо env/.env подкладываем путь до своего файла с настройками запуска приложения вида:
`DB_HOST=host.docker.internal
DB_PORT=5433
DB_USER=practicum_user
DB_PASSWORD=mysecretpassword
CATALOG_CSV='C:\Users\Home\catalog.csv'`

Запуск БД из корня проекта:
```bash
docker run --name yp-database --rm --env-file env/.env -v postgres_data:/var/lib/postgresql/data -p 5433:5432 postgres:16
```

Вместо env/.env подкладываем путь до своего файла с настройками БД вида:
`POSTGRES_USER=user
POSTGRES_PASSWORD=password'`

Накат БД выполняется автоматически при сборке проекта. Тестовая БД накатывается при прогоне интеграционных тестов.