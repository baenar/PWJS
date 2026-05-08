CREATE TABLE cities (
  id      INTEGER PRIMARY KEY AUTOINCREMENT,
  name    TEXT    NOT NULL,
  country TEXT    NOT NULL
);

INSERT INTO cities (name, country) VALUES ('Warszawa', 'PL');
INSERT INTO cities (name, country) VALUES ('Kraków', 'PL');
INSERT INTO cities (name, country) VALUES ('Wrocław', 'PL');
INSERT INTO cities (name, country) VALUES ('Gdańsk', 'PL');
INSERT INTO cities (name, country) VALUES ('Poznań', 'PL');
INSERT INTO cities (name, country) VALUES ('Łódź', 'PL');
