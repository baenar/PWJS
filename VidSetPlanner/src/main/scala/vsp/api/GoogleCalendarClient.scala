package vsp.api

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.{Calendar, CalendarScopes}
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import vsp.api.CalendarEventGoogleOps.*
import vsp.model.CalendarEvent

import java.io.FileInputStream
import java.time.{LocalDateTime, ZoneId}
import java.time.temporal.ChronoUnit
import java.util.Collections
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try, Using}

object GoogleCalendarClient {
  private def buildService(credentialsPath: String): Calendar = {
    val credentials = Using.resource(new FileInputStream(credentialsPath)) { stream =>
      GoogleCredentials
        .fromStream(stream)
        .createScoped(Collections.singletonList(CalendarScopes.CALENDAR))
    }
    val transport   = GoogleNetHttpTransport.newTrustedTransport()
    val jsonFactory = GsonFactory.getDefaultInstance
    new Calendar.Builder(transport, jsonFactory, new HttpCredentialsAdapter(credentials))
      .setApplicationName("VidSetPlanner")
      .build()
  }

  private def toDateTime(ldt: LocalDateTime): DateTime = {
    new DateTime(ldt.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli)
  }

  // Pobiera eventy z Google Calendar w podanym zakresie czasowym.
  // credentialsPath - ścieżka do pliku JSON z kluczem service account
  // calendarAccount - adres email kalendarza
  def fetchEvents(
    credentialsPath: String,
    calendarAccount: String,
    from: LocalDateTime,
    to: LocalDateTime
  ): Try[List[CalendarEvent]] = {
    Try {
      val service = buildService(credentialsPath)
      val result = service.events().list(calendarAccount)
        .setTimeMin(toDateTime(from))
        .setTimeMax(toDateTime(to))
        .setSingleEvents(true)
        .setOrderBy("startTime")
        .execute()

      Option(result.getItems)
        .map(_.asScala.toList)
        .getOrElse(List.empty)
        .map(CalendarEvent.fromGoogleEvent)
    }
  }

  // Dodaje event do Google Calendar. Zwraca ID eventu po stronie Google.
  def addEvent(credentialsPath: String, calendarId: String, event: CalendarEvent): Try[String] = {
    Try {
      val service = buildService(credentialsPath)
      val created = service.events().insert(calendarId, event.toGoogleEvent).execute()
      created.getId
    }
  }

  // Wysyła wiele eventów naraz, budując service tylko raz
  // Zwraca wynik (ID lub błąd) dla każdego eventu w tej samej kolejności.
  def addEvents(
    credentialsPath: String,
    calendarId: String,
    events: List[CalendarEvent]
  ): List[Try[String]] = {
    Try(buildService(credentialsPath)) match {
      case Failure(ex) => events.map(_ => Failure(ex))
      case Success(service) =>
        events.map { event =>
          Try {
            service.events().insert(calendarId, event.toGoogleEvent).execute().getId
          }
        }
    }
  }

  // Szuka ID eventu po stronie Google po naturalnym kluczu (tytuł + czas startu).
  // Model nie trzyma googleId, więc dopasowujemy event przeszukując dany dzień.
  def findEventId(
    credentialsPath: String,
    calendarId: String,
    title: String,
    start: LocalDateTime
  ): Try[Option[String]] = {
    Try {
      val service  = buildService(credentialsPath)
      val dayStart = start.toLocalDate.atStartOfDay()
      val dayEnd   = dayStart.plusDays(1)

      val result = service.events().list(calendarId)
        .setTimeMin(toDateTime(dayStart))
        .setTimeMax(toDateTime(dayEnd))
        .setSingleEvents(true)
        .setOrderBy("startTime")
        .execute()

      Option(result.getItems)
        .map(_.asScala.toList)
        .getOrElse(List.empty)
        .find { gEvent =>
          val parsed = CalendarEvent.fromGoogleEvent(gEvent)
          parsed.title.trim == title.trim &&
            parsed.startTime.truncatedTo(ChronoUnit.MINUTES) ==
              start.truncatedTo(ChronoUnit.MINUTES)
        }
        .map(_.getId)
    }
  }

  // Nadpisuje istniejący event na Google Calendar.
  def updateEvent(
    credentialsPath: String,
    calendarId: String,
    eventId: String,
    event: CalendarEvent
  ): Try[Unit] = {
    Try {
      val service = buildService(credentialsPath)
      service.events().update(calendarId, eventId, event.toGoogleEvent).execute()
    }.map(_ => ())
  }

  // Usuwa event z Google Calendar.
  def deleteEvent(credentialsPath: String, calendarId: String, eventId: String): Try[Unit] = {
    Try {
      val service = buildService(credentialsPath)
      service.events().delete(calendarId, eventId).execute()
    }.map(_ => ())
  }
}
