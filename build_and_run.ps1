# Файл: build_and_run.ps1
# Описание: Скрипт для очистки, сборки проекта и запуска Docker-контейнера

Write-Host "Выполняю gradle clean..."
gradle clean
if ($LASTEXITCODE -ne 0) {
    Write-Host "Ошибка при выполнении gradle clean" -ForegroundColor Red
    exit 1
}

Write-Host "Выполняю gradle build (без тестов)..."
gradle build -x test
if ($LASTEXITCODE -ne 0) {
    Write-Host "Ошибка при выполнении gradle build" -ForegroundColor Red
    exit 1
}

Write-Host "Удаляю старый образ Docker..."
docker rmi task.manager:v1.2
# Игнорируем ошибку, если образ не существует
if ($LASTEXITCODE -ne 0) {
    Write-Host "Образ не найден (это нормально при первом запуске)" -ForegroundColor Yellow
}

Write-Host "Собираю новый образ Docker..."
docker build -t task.manager:v1.2 .
if ($LASTEXITCODE -ne 0) {
    Write-Host "Ошибка при сборке Docker-образа" -ForegroundColor Red
    exit 1
}

Write-Host "Запускаю контейнеры через docker-compose..."
docker compose up
if ($LASTEXITCODE -ne 0) {
    Write-Host "Ошибка при запуске docker-compose" -ForegroundColor Red
    exit 1
}