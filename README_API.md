git # Bank Cards API Documentation

## Доступ к документации

После запуска приложения документация доступна по адресам:

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api/v3/api-docs
- **Custom Docs**: http://localhost:8080/api/docs/openapi.yaml

## Аутентификация

1. Получите JWT токен через `/auth/login`
2. Используйте токен в заголовке: `Authorization: Bearer <token>`

## Основные endpoints

### Для пользователей (USER):
- `GET /cards/my-cards` - мои карты с пагинацией
- `POST /cards/{id}/block` - заблокировать свою карту
- `POST /transactions/transfer` - перевод между своими картами

### Для администраторов (ADMIN):
- `GET /cards/admin/all-cards` - все карты в системе
- `POST /cards` - создать новую карту
- `PUT /cards/{id}/status` - изменить статус любой карты

## Примеры запросов

### Логин:
```json
POST /auth/login
{
  "username": "user123",
  "password": "password123"
}