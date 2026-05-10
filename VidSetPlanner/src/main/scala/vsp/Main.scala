package vsp

import vsp.api.GoogleCalendarClient
import vsp.core.CalendarEventService
import vsp.environment.{EnvKeys, EnvLoader}
import vsp.model.{CalendarEvent, City}
import vsp.persistence.{CityRepository, FlywayMigrator}

import java.time.LocalDateTime
import scala.util.{Success, Failure}

object Main {
  def main(args: Array[String]): Unit = {
    FlywayMigrator.migrate()

    val cities = CityRepository.findAll()
    val warszawa = cities.find(_.name == "Warszawa").getOrElse(City(1, "Warszawa", "PL"))

    val event = CalendarEvent.create(
      title = "Sesja zdjęciowa - Park Saski",
      city = warszawa,
      description = "!ważne!",
      startTime = LocalDateTime.now().plusDays(3),
      endTime = LocalDateTime.now().plusDays(3).plusHours(4)
    )

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
}