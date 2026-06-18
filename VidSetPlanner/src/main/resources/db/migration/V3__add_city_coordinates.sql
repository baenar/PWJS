-- Tabela cities staje się cache'em geokodowania (Open-Meteo geocoding API),
-- a nie listą do wyboru. Każde miasto trzyma swoje współrzędne.
ALTER TABLE cities ADD COLUMN latitude  REAL;
ALTER TABLE cities ADD COLUMN longitude REAL;

-- Stare miasta nie mają współrzędnych - resetujemy tabelę
DELETE FROM cities;
