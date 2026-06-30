# VidSetPlanner

## Opis Projektu
VidSetPlanner to aplikacja desktopowa stworzona z myślą o twórcach wideo, fotografach i realizatorach. Służy do kompleksowego planowania sesji nagraniowych (np. pod filmy na platformy społecznościowe, teledyski czy sesje zdjęciowe). Aplikacja funkcjonuje jako zaawansowany planer/kalendarz, który pozwala nie tylko zapisać termin, ale również automatyzuje część przygotowań logistycznych.

Decyzja technologiczna: Zgodnie z analizą ryzyka i sugestiami, projekt realizowany jest jako aplikacja desktopowa z wykorzystaniem biblioteki **ScalaFX/JavaFX**, co pozwoli na stworzenie responsywnego i funkcjonalnego interfejsu użytkownika.

## Główne Funkcjonalności
* **Zarządzanie Wydarzeniami:** Dodawanie, edycja i usuwanie sesji w kalendarzu. Każde wydarzenie przechowuje informacje o:
  * Dacie i dokładnym czasie,
  * Lokalizacji (miejscu nagrań/zdjęć),
  * Opisie (np. lista potrzebnego sprzętu, scenariusz).
* **Automatyczna Pogoda (API):** System na podstawie wprowadzonej lokalizacji i czasu automatycznie odpytuje zewnętrzne API pogodowe, dostarczając twórcy kluczowych informacji o warunkach atmosferycznych planowanych na czas sesji.
* **(Planowane) Integracje Zewnętrzne:** Potencjalnie (w miarę możliwości), planujemy dodanie integracji z popularnymi rozwiązaniami takimi jak Google Calendar oraz Apple Calendar w celu synchronizacji harmonogramu.

## Architektura i Podział Modułów
Aplikacja została podzielona na niezależne moduły, aby ułatwić testowanie i równoległą pracę:
1. **Moduł UI (Widok/Interfejs):** Odpowiada za wyświetlanie kalendarza, formularzy dodawania wydarzeń oraz prezentację danych pogodowych.
2. **Moduł Core/Logiki (Kontroler):** Zarządza cyklem życia wydarzeń, walidacją danych wprowadzanych przez użytkownika.
3. **Moduł API (Integracje):** Warstwa odpowiedzialna za komunikację sieciową - odpytywanie REST API o pogodę (docelowo parsowanie danych z kalendarzy Google/Apple).
4. **Moduł Persystencji (Dane):** Zarządzanie lokalnym zapisem i odczytem zaplanowanych wydarzeń (do lokalnej bazy danych).

## Uruchomienie Projektu
Wymagania do uruchomienia lokalnego:
* JDK 21,
* sbt 1.10.x.

Wejście do katalogu aplikacji:
```bash
cd VidSetPlanner
```

Uruchomienie testów:
```bash
sbt test
```

Kompilacja projektu:
```bash
sbt compile
```

Uruchomienie aplikacji:
```bash
sbt run
```

Projekt można też uruchamiać przez Dockera, bez instalowania sbt lokalnie:
```bash
docker compose run --rm test
docker compose run --rm compile
docker compose run --rm shell
```

Przykładowa konfiguracja środowiska znajduje się w pliku `.env.example`. Można tam ustawić m.in. dane do Google Calendar oraz opcjonalny URL bazy SQLite przez `VSP_DB_URL`.

## Zespół i Podział Obowiązków
* **Osoba 1:** * Integracja z zewnętrznym API pogodowym (komunikacja HTTP, parsowanie JSON).
  * Moduł persystencji danych (zapis/odczyt wydarzeń).
  * (Opcjonalnie) Research i implementacja synchronizacji z Google/Apple Calendar.
* **Osoba 2:** * Projekt i implementacja interfejsu użytkownika (ScalaFX/JavaFX).
  * Budowa widoku kalendarza oraz logiki podłączania wydarzeń pod konkretne dni.
  * Moduł zarządzania wydarzeniami i walidacja formularzy.

## Konwencje i Jakość Kodu
* **Testowanie:** Logika biznesowa (szczególnie moduł zarządzania czasem i zdarzeniami oraz parsowanie danych z API) będzie pokryta testami jednostkowymi.
* **UI:** Skupiamy się na tym, aby interfejs był przede wszystkim przejrzysty i w pełni funkcjonalny, realizując postawione zadania bez zbędnych komplikacji wizualnych.
