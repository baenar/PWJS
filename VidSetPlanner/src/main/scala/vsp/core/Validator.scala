package vsp.core

import vsp.model.CalendarEvent
import java.time.LocalDateTime

object Validator {
  def validate(event: CalendarEvent): Either[String, CalendarEvent] = event match {
    case e if e.title.trim.isEmpty                  => Left("Tytuł nie może być pusty")
    case e if e.city.id <= 0                        => Left("Miasto musi być wybrane")
    case e if e.startTime.isBefore(LocalDateTime.now()) => Left("Data rozpoczęcia nie może być w przeszłości")
    case e if !e.endTime.isAfter(e.startTime)       => Left("Czas zakończenia musi być po czasie rozpoczęcia")
    case validEvent                                 => Right(validEvent)
  }
}
