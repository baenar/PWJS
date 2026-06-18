/*package vsp

import vsp.api.{GoogleCalendarClient, WeatherClient, WeatherResult}
import vsp.core.{CalendarEventService, CityService}
import vsp.environment.{EnvKeys, EnvLoader}
import vsp.model.CalendarEvent
import vsp.persistence.FlywayMigrator

import java.time.LocalDateTime
import scala.util.{Success, Failure}

object Main {
  def main(args: Array[String]): Unit = {
    FlywayMigrator.migrate()

    val city = CityService.resolveCity("Warszawa") match {
      case Right(c)  => c
      case Left(err) =>
        println(s"Nie udało się ustalić miasta: $err")
        return
    }

    val baseEvent = CalendarEvent.create(
      title = "Sesja zdjęciowa - Park Saski",
      city = city,
      description = "!ważne!",
      startTime = LocalDateTime.now().plusDays(3),
      endTime = LocalDateTime.now().plusDays(3).plusHours(4)
    )

    val event = WeatherClient.getWeatherByCityAndDate(city, baseEvent.startTime, baseEvent.lastWeatherUpdate) match {
      case WeatherResult.Fetched(w, t, at) =>
        println(f"Pobrano pogodę: $w, $t%.1f°C")
        baseEvent.copy(weather = Some(w), temperature = Some(t), lastWeatherUpdate = Some(at))
      case WeatherResult.SkippedPastDate => println("Pogoda pominięta: data w przeszłości"); baseEvent
      case WeatherResult.SkippedTooFar   => println("Pogoda pominięta: data dalej niż tydzień"); baseEvent
      case WeatherResult.SkippedFresh    => println("Pogoda pominięta: odświeżano mniej niż 24h temu"); baseEvent
      case WeatherResult.Error(msg)      => println(s"Błąd pogody: $msg"); baseEvent
    }

    CalendarEventService.addEvent(event) match {
      case Right(_)    => println("Wydarzenie dodane.")
      case Left(error) => println(s"Błąd: $error")
    }

    val events = CalendarEventService.getAllEvents()
    println(s"Wszystkie wydarzenia (${events.length}):")
    events.foreach(e =>
      println(e)
    )

    val googleEvents = GoogleCalendarClient.fetchEvents(
      EnvLoader.get(EnvKeys.GoogleCalendarKey),
      EnvLoader.get(EnvKeys.GoogleCalendarEmail),
      LocalDateTime.now(),
      LocalDateTime.now().plusDays(10)
    )
    println("Google events:")
    googleEvents match {
      case Success(events) if events.isEmpty =>
        println("Brak wydarzeń w wybranym przedziale czasu.")
      case Success(events) =>
        events.foreach { e =>
          println(e)
        }
      case Failure(exception) =>
        println(s"Błąd podczas pobierania z Google API: ${exception.getMessage}")
    }
  }

  scene = new Scene {
    root = new MainView() // Tu wstawiamy nasz nowy widok
    fill = scalafx.scene.paint.Color.rgb(44, 62, 80) // Ciemne tło
  }
}*/

package vsp

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.Includes._
import scalafx.scene.paint.Color
import vsp.ui.MainView
import vsp.persistence.FlywayMigrator
import vsp.core.{CalendarEventService, CityService} // Dodałem CityService
import vsp.model.{CalendarEvent, City}
import vsp.persistence.CityRepository
import vsp.api.{WeatherClient, WeatherResult, GoogleCalendarClient} // Brakujące API
import vsp.environment.{EnvKeys, EnvLoader} // Brakujące zmienne środowiskowe
import java.time.LocalDateTime
import scala.util.{Try, Success, Failure} // Brakująca obsługa błędów

object Main extends JFXApp3 {

  override def start(): Unit = {
    // 1. Migracja bazy danych
    FlywayMigrator.migrate()

    // 2. Pobieranie / Testowanie logiki API (tylko do konsoli)
    val city = CityService.resolveCity("Warszawa") match {
      case Right(c)  => c
      case Left(err) =>
        println(s"Nie udało się ustalić miasta: $err")
        return
    }

    val baseEvent = CalendarEvent.create(
      title = "Sesja zdjęciowa - Park Saski",
      city = city,
      description = "!ważne!",
      startTime = LocalDateTime.now().plusDays(3),
      endTime = LocalDateTime.now().plusDays(3).plusHours(4)
    )

    val event = WeatherClient.getWeatherByCityAndDate(city, baseEvent.startTime, baseEvent.lastWeatherUpdate) match {
      case WeatherResult.Fetched(w, t, at) =>
        println(f"Pobrano pogodę: $w, $t%.1f°C")
        baseEvent.copy(weather = Some(w), temperature = Some(t), lastWeatherUpdate = Some(at))
      case WeatherResult.SkippedPastDate => println("Pogoda pominięta: data w przeszłości"); baseEvent
      case WeatherResult.SkippedTooFar   => println("Pogoda pominięta: data dalej niż tydzień"); baseEvent
      case WeatherResult.SkippedFresh    => println("Pogoda pominięta: odświeżano mniej niż 24h temu"); baseEvent
      case WeatherResult.Error(msg)      => println(s"Błąd pogody: $msg"); baseEvent
    }

    CalendarEventService.addEvent(event) match {
      case Right(_)    => println("Baza danych: Wydarzenie dodane pomyślnie.")
      case Left(error) => println(s"Baza danych: Błąd: $error")
    }

    val events = CalendarEventService.getAllEvents()
    println(s"Wszystkie wydarzenia (${events.length}):")
    events.foreach(e => println(e))

    // 3. Testowanie Google API
    try {
      val googleEvents = GoogleCalendarClient.fetchEvents(
        EnvLoader.get(EnvKeys.GoogleCalendarKey),
        EnvLoader.get(EnvKeys.GoogleCalendarEmail),
        LocalDateTime.now(),
        LocalDateTime.now().plusDays(10)
      )
      println("Google events:")
      googleEvents match {
        case Success(evs) if evs.isEmpty =>
          println("Brak wydarzeń w wybranym przedziale czasu.")
        case Success(evs) =>
          evs.foreach { e => println(e) }
        case Failure(exception) =>
          println(s"Błąd podczas pobierania z Google API: ${exception.getMessage}")
      }
    } catch {
      case e: Exception => println(s"Google API pominięte (brak pliku .env): ${e.getMessage}")
    }

    // 4. URUCHOMIENIE INTERFEJSU (Tego brakowało w nowej wersji!)
    stage = new JFXApp3.PrimaryStage {
      title = "VidSet Planner"
      scene = new Scene(1200, 800) {
        root = new MainView() 
      }
    }
  }
}