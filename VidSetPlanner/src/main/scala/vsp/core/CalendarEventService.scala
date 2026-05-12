package vsp.core

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
          case Success(_)  => Right(())
          case Failure(ex) => Left(s"Błąd zapisu: ${ex.getMessage}")
        }
      case Left(err) => Left(err)
    }
  }

  def removeEvent(id: Int): Either[String, Unit] = {
    EventRepository.deleteById(id) match {
      case Success(_)  => Right(())
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
    vsp.core.Validator.validate(event).flatMap { _ =>
      // Jeśli walidacja przejdzie, wysyłamy do repozytorium
      EventRepository.update(event) match {
        case scala.util.Success(_)  => Right(())
        case scala.util.Failure(ex) => Left(s"Błąd bazy danych: ${ex.getMessage}")
      }
    }
  }
}
