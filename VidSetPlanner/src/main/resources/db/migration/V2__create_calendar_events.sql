CREATE TABLE calendar_events (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  title       TEXT    NOT NULL,
  city_id     INTEGER NOT NULL REFERENCES cities(id),
  description TEXT,
  start_time  TEXT    NOT NULL,
  end_time    TEXT    NOT NULL
);
