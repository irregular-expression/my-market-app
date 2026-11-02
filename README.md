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
Шаг 2. Запустить контейнер из образа
```bash
docker run --name mymarket_container --add-host=host.docker.internal:host-gateway -p 8080:8080 mymarket_image:latest
```

Запуск БД из корня проекта:
```bash
docker run --name yp-database --rm --env-file env/.env -v postgres_data:/var/lib/postgresql/data -p 5433:5432 postgres:16
```

Вместо env/.env подкладываем путь до своего файла с настройками БД вида:
`POSTGRES_USER=user
POSTGRES_PASSWORD=password`

Накат БД выполняется автоматически при сборке проекта. Тестовая БД накатывается при прогоне интеграционных тестов.

Заведение ассортимента товаров в базе приложения для начала работы выходит за рамки задания, но после успешной сборки можно воспользоваться, 
например, таким скриптом (не уверен, что целесообразно выносить это в миграцию, картинки в статику также не выкладывал) :
```sql
BEGIN;

set local search_path to market;

TRUNCATE TABLE items RESTART IDENTITY CASCADE;

INSERT INTO items (title, description, img_path, price) VALUES
('Смартфон Pro X', 'Флагманский смартфон с камерой 200 МП и экраном 120 Гц.', '/static/images/smartphone_pro_x.png', 89900),
('Ноутбук UltraBook', 'Лёгкий ноутбук для работы: 16 ГБ ОЗУ, SSD 512 ГБ, процессор Core i5.', '/static/images/ultrabook.png', 74500),
('Беспроводные наушники', 'Наушники с шумоподавлением и автономностью до 30 часов.', '/static/images/headphones.png', 12990),
('Умные часы Active', 'Фитнес‑трекер, мониторинг сна, водонепроницаемость, GPS.', '/static/images/smartwatch_active.png', 9990),
('Планшет TabMax', 'Планшет 11 дюймов, 8 ГБ ОЗУ, поддержка стилуса и клавиатуры.', '/static/images/tabmax.png', 32500),
('Фотокамера ZoomPro', 'Беззеркальная камера, 4K‑видео, матрица 24 МП, стабилизация.', '/static/images/camerazoom.png', 67800),
('Игровая консоль GameBox', 'Консоль с поддержкой 4K и HDR, в комплекте 2 геймпада.', '/static/images/gamebox.png', 44990),
('Клавиатура Mechanical', 'Механическая клавиатура с RGB‑подсветкой и переключателями Blue.', '/static/images/keyboard_mech.png', 5990),
('Веб‑камера HD', 'Веб‑камера 1080p с автофокусом и встроенным микрофоном.', '/static/images/webcam_hd.png', 3490),
('Внешний SSD 1 ТБ', 'Портативный SSD со скоростью чтения до 1050 МБ/с.', '/static/images/ssd_1tb.png', 8790);

COMMIT;
```