/*
* Liquibase миграция v 1.0
* Task Manager Bot
* Инициализация БД
* Всего 3 таблицы
*/

CREATE TABLE users(
  id serial PRIMARY KEY NOT NULL,
  username VARCHAR(55) UNIQUE NOT NULL,
  email VARCHAR(100) UNIQUE,
  chat_id BIGINT NOT NULL
);

CREATE TABLE tasks(
   id serial PRIMARY KEY NOT NULL,
   name VARCHAR(55) NOT NULL,
   content VARCHAR(255) NOT NULL,
   user_username VARCHAR(55) NOT NULL,
   create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   is_completed BOOLEAN DEFAULT false,
   CONSTRAINT fk_task_user FOREIGN KEY (user_username) REFERENCES users(username)
);

CREATE TABLE reminders(
   id serial PRIMARY KEY NOT NULL,
   user_username VARCHAR(55) NOT NULL,
   task_id INTEGER NOT NULL,
   chat_id BIGINT NOT NULL,
   time TIMESTAMP NOT NULL,
   CONSTRAINT fk_reminder_user FOREIGN KEY (user_username) REFERENCES users(username),
   CONSTRAINT fk_reminder_task FOREIGN KEY (task_id) REFERENCES tasks(id)
);

CREATE INDEX idx_chat_id_users ON users(chat_id);

CREATE INDEX idx_username_name_tasks ON tasks(user_username,name);
CREATE INDEX idx_incomplete_user_tasks ON tasks(user_username) WHERE is_completed = false;

CREATE INDEX idx_user_reminders ON reminders(user_username);
CREATE INDEX idx_time_reminder ON reminders(user_username,time);

