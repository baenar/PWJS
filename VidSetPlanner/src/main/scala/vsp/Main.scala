/*package vsp

import vsp.ui.MainView
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
import vsp.core.CalendarEventService
import vsp.model.{CalendarEvent, City}
import vsp.persistence.CityRepository
import java.time.LocalDateTime

object Main extends JFXApp3 {

  override def start(): Unit = {
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
      case Right(_)    => println("Baza danych: Wydarzenie dodane pomyślnie.")
      case Left(error) => println(s"Baza danych: Błąd: $error")
    }

    stage = new JFXApp3.PrimaryStage {
      title = "VidSetPlanner - Desktop App"
      width = 900
      height = 700
      
      scene = new Scene {
        fill = Color.rgb(44, 62, 80)
        root = new MainView() // Wywołujemy Twój widok z folderu ui
      }
    }
  }
}