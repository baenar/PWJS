package vsp.core

import vsp.api.GoogleEventSync
import vsp.model.CalendarEvent
import vsp.persistence.EventRepository

import scala.util.{Failure, Success}

object CalendarEventService {

  def getAllEvents(): List[CalendarEvent] = {
    EventRepository.findAll()
  }

  def addEvent(event: CalendarEvent): Either[String, Unit] = {
    Validator.validate(event) match {
      case Right(valid) =>
        EventRepository.save(valid) match {
          case Success(_)  =>
            // Po udanym zapisie wysyłamy event także na Google Calendar.
            GoogleEventSync.onAdded(valid)
            Right(())
          case Failure(ex) => Left(s"Błąd zapisu: ${ex.getMessage}")
        }
      case Left(err) => Left(err)
    }
  }

  def updateWeather(event: CalendarEvent): Either[String, Unit] = {
    EventRepository.update(event) match {
      case Success(_)  => Right(())
      case Failure(ex) => Left(s"Błąd zapisu pogody: ${ex.getMessage}")
    }
  }

  def removeEvent(id: Int): Either[String, Unit] = {
    // Pobieramy event przed usunięciem, żeby móc go skasować również z Google.
    val event = EventRepository.findById(id)
    EventRepository.deleteById(id) match {
      case Success(_)  =>
        event.foreach(GoogleEventSync.onRemoved)
        Right(())
      case Failure(ex) => Left(s"Błąd usuwania: ${ex.getMessage}")
    }
  }

  def removeAllEvents(): Either[String, Unit] = {
    // 1. Pobieramy wszystkie aktualne eventy
    val allEvents = EventRepository.findAll()

    // 2. Mapujemy listę eventów na wyniki ich usuwania
    // Wynik to List[Either[String, Unit]]
    val results = allEvents.map(event => removeEvent(event.id))

    // 3. Sprawdzamy, czy którykolwiek z nich zwrócił błąd (Left)
    val firstError = results.collectFirst { case Left(err) => err }

    firstError match {
      case Some(error) => Left(s"Wystąpił błąd podczas masowego usuwania: $error")
      case None        => Right(())
    }
  }

  def updateEvent(event: CalendarEvent): Either[String, Unit] = {
    // Najpierw sprawdzamy, czy dane są poprawne (np. czy tytuł nie jest pusty)
    Validator.validate(event).flatMap { _ =>
      // Stan sprzed zmiany - potrzebny, żeby odnaleźć ten event po stronie Google
      // (tytuł lub czas startu mogły się zmienić).
      val oldEvent = EventRepository.findById(event.id)
      EventRepository.update(event) match {
        case Success(_) =>
          oldEvent.foreach(old => GoogleEventSync.onUpdated(old, event))
          Right(())
        case Failure(ex) => Left(s"Błąd bazy danych: ${ex.getMessage}")
      }
    }
  }
}
