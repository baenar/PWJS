package vsp.model

import java.time.LocalDateTime

case class CalendarEvent(
  id: Int,
  title: String,
  city: City,
  description: String,
  startTime: LocalDateTime,
  endTime: LocalDateTime
) {
  override def toString: String = {
    val date  = startTime.toLocalDate
    val start = startTime.toLocalTime.withSecond(0).withNano(0)
    val end   = endTime.toLocalTime.withSecond(0).withNano(0)
    s"[$id] $title @ ${city.name} ($date, $start – $end)"
  }
}

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
