# EducationSystem
This repository was created for FullStack project IITU.

## Аутентификация
Большинство endpoints требуют JWT токен в заголовке Authorization:
Authorization: Bearer ваш_jwt_токен ----> Токен получается при логине и используется для последующих запросов.

## API Endpoints
🔑 Аутентификация
POST /auth/login
Авторизация пользователя
------------------------
POST /auth/logout
Выход из системы
------------------------
GET /auth/me
Информация о текущем пользователе
------------------------

👨‍💼 Административные endpoints
POST /admin/register/teacher
Регистрация нового учителя (только для админов)
------------------------
POST /admin/register/student
Регистрация нового студента (только для админов)
------------------------
POST /admin/register/parent
Регистрация нового родителя (только для админов)
------------------------

📚 Работа с заданиями
GET /assignments
Получить все задания (для всех авторизованных пользователей)
------------------------
GET /assignments/{id}
Получить задание по ID
------------------------
GET /assignments/class/{classId}
Получить задания для конкретного класса
------------------------

👨‍🏫 Endpoints для учителей
POST /teacher/assignments
Создать новое задание (только для учителей)
------------------------
GET /teacher/assignments/my
Получить мои задания (только для учителей)
------------------------
PUT /teacher/assignments/{id}
Редактировать задание (только автор)
------------------------
DELETE /teacher/assignments/{id}
Удалить задание (только автор)

## Роли и права доступа
Роль	Доступные endpoints
Все	/auth/login, /auth/logout, /auth/me, /assignments/*
Учитель	/teacher/assignments/* (только свои задания)
Админ	/admin/register/*