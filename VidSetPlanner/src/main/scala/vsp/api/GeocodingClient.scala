package vsp.api

import org.json.JSONObject
import vsp.model.City

import java.net.URI
import java.net.URLEncoder
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets
import scala.util.{Failure, Success, Try}

// Zamienia nazwę miasta na współrzędne poprzez Open-Meteo geocoding API.
object GeocodingClient {

  private val GeocodeUrl = "https://geocoding-api.open-meteo.com/v1/search"
  private val httpClient  = HttpClient.newHttpClient()

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
