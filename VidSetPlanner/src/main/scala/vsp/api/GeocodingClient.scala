package vsp.api

import org.json.{JSONArray, JSONObject}
import vsp.model.City

import java.net.URI
import java.net.URLEncoder
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

// Zamienia nazwę miasta na współrzędne poprzez Open-Meteo geocoding API.
object GeocodingClient {

  private val GeocodeUrl   = "https://geocoding-api.open-meteo.com/v1/search"
  private val NominatimUrl = "https://nominatim.openstreetmap.org/search"
  private val httpClient   = HttpClient.newHttpClient()

  def geocode(name: String): Either[String, City] = {
    val encoded = URLEncoder.encode(name, StandardCharsets.UTF_8)
    val url     = s"$GeocodeUrl?name=$encoded&count=1&language=pl&format=json"

    val attempt = Try {
      val request  = HttpRequest.newBuilder().uri(URI.create(url)).GET().build()
      val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
      parseFirstResult(response.body())
    }

    attempt match {
      case Success(Some(city)) => Right(city)
      case Success(None)       => Left(s"Nie znaleziono miasta: $name")
      case Failure(ex)         => Left(s"Błąd geokodowania: ${ex.getMessage}")
    }
  }

  // Geokoduje pełny adres (u nas lokalizację z Google Calendar) przez Nominatim (OpenStreetMap).
  // Przyjmuje cały adres i zwraca ustrukturyzowane składniki - wyciągamy z nich samo miasto
  def geocodeAddress(address: String): Either[String, City] = {
    // Pełny adres z Google bywa zbyt szczegółowy dla Nominatim (nazwa miejsca + ulica)
    // i zwraca 0 wyników. Dlatego próbujemy pełnego adresu, a gdy pudło, ponawiamy dla
    // coraz krótszych wariantów (aż zostanie samo miasto + kraj).
    @tailrec
    def tryNext(remaining: List[String]): Either[String, City] = remaining match {
      case Nil => Left(s"Nie znaleziono miasta dla adresu: $address")
      case query :: rest =>
        queryNominatim(query) match {
          case Success(Some(city)) => Right(city)
          case Success(None)       => tryNext(rest)
          case Failure(ex)         => Left(s"Błąd geokodowania: ${ex.getMessage}")
        }
    }
    tryNext(addressVariants(address))
  }

  // Pełny adres, a potem warianty bez kolejnych początkowych segmentów,
  // aż zostaną co najmniej dwa, czyli zwykle miasto i kraj.
  private def addressVariants(address: String): List[String] = {
    val segments = address.split(",").map(_.trim).filter(_.nonEmpty).toList
    segments.indices
      .filter(i => i == 0 || segments.length - i >= 2)
      .map(i => segments.drop(i).mkString(", "))
      .toList
  }

  private def queryNominatim(query: String): Try[Option[City]] = {
    val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8)
    val url     = s"$NominatimUrl?q=$encoded&format=json&addressdetails=1&limit=1&accept-language=pl"
    Try {
      // Nominatim dopuszcza maks 1 zapytanie/s
      Thread.sleep(1000)
      val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("User-Agent", "VidSetPlanner/1.0")
        .GET()
        .build()
      val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
      parseNominatim(response.body())
    }
  }

  private def parseNominatim(body: String): Option[City] = {
    val results = new JSONArray(body)
    if (results.length() == 0) {
      None
    } else {
      val first   = results.getJSONObject(0)
      val address = first.getJSONObject("address")
      val cityKeys = List("city", "town", "village", "municipality")
      val cityName = cityKeys.collectFirst { case k if address.has(k) => address.getString(k) }

      cityName.map { name =>
        val country = if (address.has("country_code")) address.getString("country_code") else ""
        City(
          id        = 0,
          name      = name,
          country   = country,
          latitude  = first.getString("lat").toDouble,
          longitude = first.getString("lon").toDouble
        )
      }
    }
  }

  private def parseFirstResult(body: String): Option[City] = {
    val json = new JSONObject(body)
    if (!json.has("results")) {
      return None
    }
    val results = json.getJSONArray("results")
    if (results.length() == 0) {
      None
    } else {
      val first   = results.getJSONObject(0)
      val country = if (first.has("country_code")) first.getString("country_code") else ""
      Some(City(
        id        = 0,
        name      = first.getString("name"),
        country   = country,
        latitude  = first.getDouble("latitude"),
        longitude = first.getDouble("longitude")
      ))
    }
  }
}
