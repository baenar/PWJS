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
}
