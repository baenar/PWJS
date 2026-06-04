package vsp.model

import java.time.LocalDateTime

case class CalendarEvent(
  id: Int,
  title: String,
  city: City,
  description: String,
  startTime: LocalDateTime,
  endTime: LocalDateTime,
  weather: Option[String] = None,
  temperature: Option[Double] = None,
  lastWeatherUpdate: Option[LocalDateTime] = None
) {
  override def toString: String = {
    val date  = startTime.toLocalDate
    val start = startTime.toLocalTime.withSecond(0).withNano(0)
    val end   = endTime.toLocalTime.withSecond(0).withNano(0)
    val weatherStr = weather match {
      case Some(w) =>
        val temp = temperature.map(t => f", $t%.1f°C").getOrElse("")
        s" | pogoda: $w$temp"
      case None => ""
    }
    s"[$id] $title @ ${city.name} ($date, $start - $end)$weatherStr"
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
