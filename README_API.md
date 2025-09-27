# Bank Cards API Documentation

## 📋 Общая информация
- **Base URL**: `http://localhost:8080`
- **Аутентификация**: JWT Token
- **Формат данных**: JSON

## 🔐 Аутентификация

### Регистрация нового пользователя
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

**Response:**
```json
{
  "id": 7,
  "username": "Test",
  "email": "test@string",
  "firstName": "Test",
  "lastName": "Test",
  "phoneNumber": "79111231234",
  "status": "ACTIVE",
  "roles": ["ROLE_USER"]
}
```

### Получение токена
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

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@bank.com",
  "userId": 1
}
```

**Использование токена:**
```
Authorization: Bearer <your_token>
```

## 👤 Роли и права доступа

### Администратор (ADMIN)
- ✅ Полный доступ ко всем операциям
- ✅ Создание/удаление счетов и карт
- ✅ Просмотр всех пользователей
- ❌ Переводы между чужими счетами и картами

### Пользователь (USER)
- ✅ Мой профиль, мои счета, мои карты
- ✅ Переводы между своими счетами/картами
- ❌ Создание/удаление счетов и карт
- ❌ Просмотр других пользователей

## 💳 Управление счетами (только ADMIN)

### Создание счета
```http
POST /accounts
```
**Request:**
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

## 💳 Управление картами (только ADMIN)

### Создание карты
```http
POST /cards
```
**Request:**
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

## 💸 Переводы средств

### Перевод между счетами
```http
POST /transactions/transfer
```
**Request:**
```json
{
  "fromAccountNumber": "40817810000000000002",
  "toAccountNumber": "40817810000000000001",
  "amount": 1000,
  "description": "test transfer acc_to_acc"
}
```

### Перевод между картами
```http
POST /transactions/card-to-card
```
**Request:**
```json
{
  "fromCardId": 2,
  "toCardId": 5,
  "amount": 450.00,
  "description": "test transfer card_to_card"
}
```

### Перевод с карты на счет
```http
POST /transactions/card-to-account
```
**Request:**
```json
{
  "fromCardId": 2,
  "toAccountNumber": "40817810000000000001",
  "amount": 300.00,
  "description": "test transfer card_to_acc"
}
```

## 👤 Пользовательские endpoints

### Мой профиль
```http
GET /api/users/my-profile
```

### Мои счета
```http
GET /accounts/my-accounts
```

### Мои карты
```http
GET /cards/my-cards
```

## 🔧 Тестовые данные

### Учетные данные по умолчанию:
- **ADMIN**: `admin` / `admin123`
- **USER**: `Test` / `admin123` (после регистрации)
- **USER**: 'ivanov' / `admin123`
- **USER**: 'petrov' / `admin123`
- **USER**: 'sidorov' / `admin123`

### Примеры ID для тестирования:
- Пользователи: 1 (admin), 2 (ivanov), 3 (petrov), 4 (sidorov), 5 (Test)
- Посмотреть счета: GET/users/with-accounts (admin), GET/users/my-profile (user)
- Карты: 

## 📊 Коды ответов

- `200` - Успех
- `400` - Ошибка валидации
- `401` - Не авторизован
- `403` - Доступ запрещен
- `404` - Ресурс не найден
- `500` - Внутренняя ошибка сервера

## 🔗 Документация API

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

---

*Для доступа к защищенным endpoints используйте JWT токен в заголовке Authorization.*