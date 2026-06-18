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
import java.util.Collections
import scala.jdk.CollectionConverters.*
import scala.util.{Try, Using}

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
}
