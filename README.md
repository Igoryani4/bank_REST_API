# Система управления банковскими картами

## 🎯 О проекте

**Bank Cards Management System** - это backend-приложение на Spring Boot для безопасного управления банковскими картами, счетами и финансовыми операциями. Система обеспечивает ролевой доступ, шифрование данных и полный цикл операций с банковскими продуктами.

---

## 📋 Содержание
- [Технологии](#-технологии)
- [Функциональность](#-функциональность)
- [Установка и запуск](#-установка-и-запуск)
- [API Документация](#-api-документация)
- [Архитектура](#-архитектура)
- [Безопасность](#-безопасность)

---

## 🛠 Технологии

- **Java 17+** - основной язык разработки
- **Spring Boot 3.x** - фреймворк
- **Spring Security + JWT** - аутентификация и авторизация
- **Spring Data JPA** - работа с базой данных
- **PostgreSQL** - реляционная база данных
- **Liquibase** - управление миграциями БД
- **Maven** - сборка проекта
- **Docker** - контейнеризация
- **Swagger/OpenAPI 3** - документация API

---

## ⚙️ Функциональность

### 👨‍💼 Администратор (ROLE_ADMIN)
- ✅ Полное управление пользователями
- ✅ Создание/удаление счетов и карт
- ✅ Просмотр всех операций в системе
- ✅ Блокировка/активация карт и счетов
- ✅ Управление статусами продуктов

### 👤 Пользователь (ROLE_USER)
- ✅ Просмотр своего профиля
- ✅ Управление своими счетами и картами
- ✅ Переводы между своими счетами/картами
- ✅ Блокировка своих карт
- ✅ Просмотр истории операций

---

## 🚀 Установка и запуск

### Требования
- Java 17+
- Maven 3.6+
- PostgreSQL 14+
- Docker (опционально)

### Локальный запуск

1. **Клонирование репозитория**
```bash
git clone <repository-url>
cd bank-cards-system
```
2. **Запуск приложения**
```bash
mvn spring-boot:run
```

### Docker запуск
```bash
docker-compose up -d
```

---

## 📚 API Документация

### 🔐 Аутентификация

#### Регистрация нового пользователя
```http
POST /auth/signup
```
**Request:**
```json
{
  "username": "Test",
  "email": "test@string",
  "password": "admin123",
  "firstName": "Test",
  "lastName": "Test", 
  "phoneNumber": "79111231234"
}
```

#### Получение JWT токена
```http
POST /auth/signin
```
**Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Использование токена:**
```
Authorization: Bearer <your_token>
```

### 💳 Управление счетами (ADMIN only)

#### Создание счета
```http
POST /accounts
```
```json
{
  "balance": 1000000,
  "currency": "RUB",
  "type": "CURRENT",
  "status": "ACTIVE",
  "user": {
    "id": 2
  }
}
```

### 💳 Управление картами (ADMIN only)

#### Создание карты
```http
POST /cards
```
```json
{
  "type": "DEBIT",
  "status": "ACTIVE",
  "dailyLimit": 1000,
  "cardHolderName": "IVAN IVANOV",
  "account": {
    "id": 5
  }
}
```

### 💸 Переводы средств

#### Перевод между счетами
```http
POST /transactions/transfer
```
```json
{
  "fromAccountNumber": "40817810000000000002",
  "toAccountNumber": "40817810000000000001",
  "amount": 1000,
  "description": "test transfer"
}
```

#### Перевод между картами
```http
POST /transactions/card-to-card
```
```json
{
  "fromCardId": 2,
  "toCardId": 5,
  "amount": 450.00,
  "description": "card to card transfer"
}
```

#### Перевод с карты на счет
```http
POST /transactions/card-to-account
```
```json
{
  "fromCardId": 2,
  "toAccountNumber": "40817810000000000001",
  "amount": 300.00,
  "description": "card to account transfer"
}
```

### 👤 Пользовательские endpoints

#### Мой профиль
```http
GET /api/users/my-profile
```

#### Мои счета
```http
GET /accounts/my-accounts
```

#### Мои карты
```http
GET /cards/my-cards
```

---

## 🏗 Архитектура

### Структура проекта
```
src/
├── main/
│   ├── java/
│   │   └── com/example/bankcards/
│   │       ├── controller/     # REST контроллеры
│   │       ├── service/        # Бизнес-логика
│   │       ├── repository/     # Data Access Layer
│   │       ├── entity/         # JPA сущности
│   │       ├── dto/           # Data Transfer Objects
│   │       ├── security/      # Конфигурация безопасности
│   │       └── config/        # Конфигурации
│   └── resources/
│       ├── db/migration/      # Liquibase миграции
│       └── application.yml
```

### Основные сущности
- **User** - пользователь системы
- **Account** - банковский счет
- **Card** - банковская карта
- **Transaction** - финансовые операции

---

## 🔒 Безопасность

### Особенности реализации
- 🔐 **JWT аутентификация** с refresh токенами
- 🛡 **Шифрование данных** карт (номера, CVV)
- 👥 **Ролевая модель доступа** (ADMIN/USER)
- 📊 **Маскирование номеров** карт в ответах API
- ⚠️ **Валидация операций** перед выполнением

### Защита данных
- Номера карт хранятся в зашифрованном виде
- CVV код шифруется при сохранении
- Баланс защищен от отрицательных значений
- Проверка прав доступа к операциям

---

## 📊 Тестовые данные

### Учетные записи по умолчанию
- **ADMIN**: `admin` / `admin123`
- **USER**: `ivanov` / `admin123`
- **USER**: `petrov` / `admin123`
- **USER**: `sidorov` / `admin123`

### Документация
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

___

## 👥 Разработка

### Сборка проекта
```bash
mvn clean package
```

### Запуск тестов
```bash
mvn test
```

### Code style
Проект использует стандартные code style conventions Java и Spring.

---

## 📄 Лицензия

Проект разработан в учебных целях.