package vsp.model

import java.time.LocalDateTime

case class CalendarEvent(
  id: Int,
  title: String,
  city: City,
  description: String,
  startTime: LocalDateTime,
  endTime: LocalDateTime
)

object CalendarEvent {
  def create(
    title: String,
    city: City,
    description: String,
    startTime: LocalDateTime,
    endTime: LocalDateTime
  ): CalendarEvent = {
    CalendarEvent(0, title, city, description, startTime, endTime)
  }
}
