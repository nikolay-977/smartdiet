# SmartDiet

##  Архитектура

smartdiet/
├── products-service/ # Сервис информации о продуктах и нутриентах - в работе
├── api-gateway/ # API Gateway (Spring Cloud Gateway) - в беклоге
├── auth-service/ # Сервис аутентификации и авторизации - в беклоге
├── common-library/ # Общие DTO, конфигурации, утилиты - в беклоге
└── docker-compose.yml # Docker Compose для запуска всей инфраструктуры - в беклоге

## Как запустить?

### 1. Клонируйте репозиторий
```bash
git clone https://github.com/nikolay-977/smartdiet
cd smartdiet
```

### 2. Соберите приложение
```bash
./gradlew :products-service:clean build
```

### 3. Запустите приложение
```bash
./gradlew :products-service:bootRun
```

### 4. Протестируйте:

#### Поиск Nutella в Open Food
```bash
curl http://localhost:8082/api/products/3017620422003
```

#### Поиск яблок в USDA - для тестирования необходимо указать USDA_API_KEY в application.yaml (Зарегистрируйтесь на https://fdc.nal.usda.gov/)
```bash
curl "http://localhost:8082/api/products/search/usda?query=apple"
```
