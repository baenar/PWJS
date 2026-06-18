package vsp.api

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.{Event, EventDateTime}
import vsp.model.{CalendarEvent, City}

import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

// Extension methods
object CalendarEventGoogleOps {
  extension (event: CalendarEvent) {
    def toGoogleEvent: Event = {
      def asEventDateTime(ldt: LocalDateTime): EventDateTime = {
        val millis = ldt.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli
        new EventDateTime().setDateTime(new DateTime(millis))
      }
      new Event()
        .setSummary(event.title)
        .setDescription(event.description)
        .setLocation(event.city.name)
        .setStart(asEventDateTime(event.startTime))
        .setEnd(asEventDateTime(event.endTime))
    }
  }

  extension (c: CalendarEvent.type) {
    def fromGoogleEvent(gEvent: Event): CalendarEvent = {
      def parseLDT(dt: DateTime): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(dt.getValue), ZoneId.systemDefault())

      def resolveStart(): LocalDateTime = {
        Option(gEvent.getStart.getDateTime) match {
          case Some(dt) => parseLDT(dt)
          case None     => LocalDate.parse(gEvent.getStart.getDate.toStringRfc3339).atStartOfDay()
        }
      }

      def resolveEnd(): LocalDateTime = {
        Option(gEvent.getEnd.getDateTime) match {
          case Some(dt) => parseLDT(dt)
          case None =>
            val endExclusive = LocalDate.parse(gEvent.getEnd.getDate.toStringRfc3339)
            endExclusive.minusDays(1).atTime(23, 59)
        }
      }

      CalendarEvent(
        id          = 0,
        title       = Option(gEvent.getSummary).getOrElse("(No title provided)"),
        city        = City(0, Option(gEvent.getLocation).getOrElse("Somewhere?"), "", 0.0, 0.0),
        description = Option(gEvent.getDescription).getOrElse(""),
        startTime   = resolveStart(),
        endTime     = resolveEnd()
      )
    }
  }
}
