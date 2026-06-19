package vsp.persistence

import vsp.model.CalendarEvent
import vsp.persistence.slickdb.SlickEventRepository

import scala.util.Try

object EventRepository {

  def findAll(): List[CalendarEvent] = {
    SlickEventRepository.findAll().getOrElse(List())
  }

  def save(event: CalendarEvent): Try[Unit] = {
    SlickEventRepository.save(event).map(_ => ())
  }

  def deleteById(id: Int): Try[Unit] = {
    SlickEventRepository.deleteById(id).map(_ => ())
  }

  def update(event: CalendarEvent): Try[Unit] = {
    SlickEventRepository.update(event).map(_ => ())
  }
}
