# Squares — multi-module project
Тестовое задание.
Проект содержит 3 модуля:
- `engine` — независимый движок игры (Java library).
- `cli` — консольное приложение (использует движок) — Задание 1.
- `server` — простой HTTP сервер на `com.sun.net.httpserver` + статический фронтенд — Задания 2 и 3.

Требования: Java 11+, Maven.

## Сборка
В корне проекта выполните:
mvn package или mvn -DskipTests package
Эта команда подтянет все зависимости.

- Запуск первого задания (cli игра) - java -jar cli/target/cli-1.0.0.jar
- Запуск сервера (второе и третье задание) - java -jar server/target/server-1.0.0.jar
