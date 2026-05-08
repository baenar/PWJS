package vsp

import vsp.core.CalendarEventService
import vsp.model.{CalendarEvent, City}
import vsp.persistence.{CityRepository, FlywayMigrator}

import java.time.LocalDateTime

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
    events.foreach { e =>
      println(s"  [${e.id}] ${e.title} @ ${e.city.name} - ${e.startTime} do ${e.endTime}")
    }
  }
}