-- Pogoda cache'owana bezpośrednio na wydarzeniu.
ALTER TABLE calendar_events ADD COLUMN weather             TEXT;
ALTER TABLE calendar_events ADD COLUMN temperature         REAL;
ALTER TABLE calendar_events ADD COLUMN last_weather_update TEXT;