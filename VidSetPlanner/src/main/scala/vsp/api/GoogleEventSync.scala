package vsp.api

import vsp.environment.{EnvKeys, EnvLoader}
import vsp.model.CalendarEvent

import java.time.LocalDateTime
import scala.util.{Failure, Success}

// Reakcja na zmiany pojedynczych wydarzeń - utrzymuje Google Calendar w zgodzie
// z lokalną bazą po dodaniu / edycji / usunięciu eventu.
// Brak konfiguracji (.env) albo błąd Google nie blokuje operacji na bazie - tylko logujemy.
object GoogleEventSync {

  private def credentials(): Option[(String, String)] = {
    val path    = EnvLoader.get(EnvKeys.GoogleCalendarKey)
    val account = EnvLoader.get(EnvKeys.GoogleCalendarEmail)
    if (path == null || account == null) None else Some((path, account))
  }

  // Okno synchronizacji jak przy starcie aplikacji: od teraz do miesiąca w przód.
  private def inRange(event: CalendarEvent): Boolean = {
    val now = LocalDateTime.now()
    !event.startTime.isBefore(now) && event.startTime.isBefore(now.plusMonths(1))
  }

  def onAdded(event: CalendarEvent): Unit = {
    if (!inRange(event)) {
      return
    }
    credentials().foreach { case (path, account) =>
      GoogleCalendarClient.addEvent(path, account, event) match {
        case Success(_)  => println(s"Dodano do Google Calendar: ${event.title}")
        case Failure(ex) => println(s"Nie dodano '${event.title}' do Google: ${ex.getMessage}")
      }
    }
  }

  def onUpdated(oldEvent: CalendarEvent, newEvent: CalendarEvent): Unit = {
    credentials().foreach { case (path, account) =>
      // Szukamy po starym kluczu (tytuł/czas mogły się zmienić).
      GoogleCalendarClient.findEventId(path, account, oldEvent.title, oldEvent.startTime) match {
        case Success(Some(id)) =>
          GoogleCalendarClient.updateEvent(path, account, id, newEvent) match {
            case Success(_)  => println(s"Zaktualizowano w Google Calendar: ${newEvent.title}")
            case Failure(ex) => println(s"Nie zaktualizowano '${newEvent.title}' w Google: ${ex.getMessage}")
          }
        case Success(None) =>
          // Nie było na Google - jeśli mieści się w oknie, dodajemy jako nowy.
          if (inRange(newEvent)) {
            onAdded(newEvent)
          } else {
            println(s"Nie znaleziono '${oldEvent.title}' w Google - pominięto aktualizację.")
          }
        case Failure(ex) =>
          println(s"Błąd szukania '${oldEvent.title}' w Google: ${ex.getMessage}")
      }
    }
  }

  def onRemoved(event: CalendarEvent): Unit = {
    credentials().foreach { case (path, account) =>
      GoogleCalendarClient.findEventId(path, account, event.title, event.startTime) match {
        case Success(Some(id)) =>
          GoogleCalendarClient.deleteEvent(path, account, id) match {
            case Success(_)  => println(s"Usunięto z Google Calendar: ${event.title}")
            case Failure(ex) => println(s"Nie usunięto '${event.title}' z Google: ${ex.getMessage}")
          }
        case Success(None) =>
          println(s"Nie znaleziono '${event.title}' w Google - nic do usunięcia.")
        case Failure(ex) =>
          println(s"Błąd szukania '${event.title}' w Google: ${ex.getMessage}")
      }
    }
  }
}
