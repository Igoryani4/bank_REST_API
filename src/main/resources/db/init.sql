
-- Создание расширения для UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Создание таблицы ролей (если еще нет)
CREATE TABLE IF NOT EXISTS roles (
                                     id SERIAL PRIMARY KEY,
                                     name VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(255)
    );

-- Вставка основных ролей
INSERT INTO roles (name, description) VALUES
                                          ('ROLE_USER', 'Обычный пользователь'),
                                          ('ROLE_ADMIN', 'Администратор системы'),
                                          ('ROLE_MODERATOR', 'Модератор')
    ON CONFLICT (name) DO NOTHING;

-- Создание тестовых пользователей (пароли зашифрованы BCrypt)
-- Пароль для всех тестовых пользователей: 'password123'

-- Администратор
INSERT INTO users (username, email, password, first_name, last_name, phone_number, status, created_at, updated_at)
VALUES (
           'admin',
           'admin@bank.com',
           '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV2UiC', -- password123
           'Администратор',
           'Системы',
           '+79160000001',
           'ACTIVE',
           CURRENT_TIMESTAMP,
           CURRENT_TIMESTAMP
       ) ON CONFLICT (username) DO NOTHING;

-- Обычные пользователи
INSERT INTO users (username, email, password, first_name, last_name, phone_number, status, created_at, updated_at)
VALUES
    (
        'ivanov',
        'ivanov@example.com',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV2UiC', -- password123
        'Иван',
        'Иванов',
        '+79161234567',
        'ACTIVE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'petrova',
        'petrova@example.com',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV2UiC', -- password123
        'Мария',
        'Петрова',
        '+79167654321',
        'ACTIVE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'sidorov',
        'sidorov@example.com',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV2UiC', -- password123
        'Алексей',
        'Сидоров',
        '+79165554433',
        'ACTIVE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ) ON CONFLICT (username) DO NOTHING;

-- Назначение ролей пользователям
-- Админ получает ROLE_ADMIN и ROLE_USER
INSERT INTO user_roles (user_id, role)
VALUES
    ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_ADMIN'),
    ((SELECT id FROM users WHERE username = 'admin'), 'ROLE_USER'),
    ((SELECT id FROM users WHERE username = 'ivanov'), 'ROLE_USER'),
    ((SELECT id FROM users WHERE username = 'petrova'), 'ROLE_USER'),
    ((SELECT id FROM users WHERE username = 'sidorov'), 'ROLE_USER')
    ON CONFLICT DO NOTHING;

-- Создание тестовых счетов
INSERT INTO accounts (account_number, balance, currency, type, status, user_id, created_at)
VALUES
    ('40817810000000000001', 100000.00, 'RUB', 'CURRENT', 'ACTIVE', (SELECT id FROM users WHERE username = 'admin'), CURRENT_TIMESTAMP),
    ('40817810000000000002', 50000.00, 'RUB', 'CURRENT', 'ACTIVE', (SELECT id FROM users WHERE username = 'ivanov'), CURRENT_TIMESTAMP),
    ('40817810000000000003', 75000.00, 'RUB', 'SAVINGS', 'ACTIVE', (SELECT id FROM users WHERE username = 'petrova'), CURRENT_TIMESTAMP),
    ('40817810000000000004', 25000.00, 'RUB', 'CURRENT', 'ACTIVE', (SELECT id FROM users WHERE username = 'sidorov'), CURRENT_TIMESTAMP)
    ON CONFLICT (account_number) DO NOTHING;

-- Создание тестовых карт
INSERT INTO cards (card_number, expiry_date, cvv, card_holder_name, type, status, daily_limit, account_id, created_at)
VALUES
    ('4111111111111111', CURRENT_DATE + INTERVAL '3 years', '123', 'ADMIN SYSTEM', 'DEBIT', 'ACTIVE', 100000.00, (SELECT id FROM accounts WHERE account_number = '40817810000000000001'), CURRENT_TIMESTAMP),
    ('4222222222222222', CURRENT_DATE + INTERVAL '3 years', '456', 'IVAN IVANOV', 'DEBIT', 'ACTIVE', 50000.00, (SELECT id FROM accounts WHERE account_number = '40817810000000000002'), CURRENT_TIMESTAMP),
    ('4333333333333333', CURRENT_DATE + INTERVAL '3 years', '789', 'MARIA PETROVA', 'CREDIT', 'ACTIVE', 100000.00, (SELECT id FROM accounts WHERE account_number = '40817810000000000003'), CURRENT_TIMESTAMP),
    ('4444444444444444', CURRENT_DATE + INTERVAL '3 years', '321', 'ALEXEY SIDOROV', 'DEBIT', 'ACTIVE', 25000.00, (SELECT id FROM accounts WHERE account_number = '40817810000000000004'), CURRENT_TIMESTAMP)
    ON CONFLICT (card_number) DO NOTHING;

-- Создание тестовых транзакций
INSERT INTO transactions (transaction_id, amount, currency, type, status, description, from_account_id, to_account_id, created_at)
VALUES
    ('TXN001', 5000.00, 'RUB', 'TRANSFER', 'COMPLETED', 'Перевод между счетами',
     (SELECT id FROM accounts WHERE account_number = '40817810000000000001'),
     (SELECT id FROM accounts WHERE account_number = '40817810000000000002'),
     CURRENT_TIMESTAMP - INTERVAL '2 days'),

    ('TXN002', 3000.00, 'RUB', 'TRANSFER', 'COMPLETED', 'Оплата услуг',
     (SELECT id FROM accounts WHERE account_number = '40817810000000000002'),
     (SELECT id FROM accounts WHERE account_number = '40817810000000000003'),
     CURRENT_TIMESTAMP - INTERVAL '1 day'),

    ('TXN003', 10000.00, 'RUB', 'DEPOSIT', 'COMPLETED', 'Пополнение счета',
     NULL,
     (SELECT id FROM accounts WHERE account_number = '40817810000000000004'),
     CURRENT_TIMESTAMP - INTERVAL '5 hours')
    ON CONFLICT (transaction_id) DO NOTHING;

-- Логирование создания тестовых данных
DO $$
BEGIN
    RAISE NOTICE 'Тестовые данные успешно созданы';
    RAISE NOTICE 'Пользователи: admin, ivanov, petrova, sidorov';
    RAISE NOTICE 'Пароль для всех: password123';
    RAISE NOTICE 'Роли: ROLE_USER, ROLE_ADMIN';
END $$;