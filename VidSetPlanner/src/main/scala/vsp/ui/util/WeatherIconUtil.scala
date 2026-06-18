package vsp.ui.util

import scalafx.scene.image.{Image, ImageView}

object WeatherIconUtil {

  // Zauważ dodany parametr 'size' z domyślną wartością 24.0
  def getWeatherIcon(weather: String, size: Double = 24.0): ImageView = {
    val w = weather.toLowerCase

    val iconName = w match {
      case x if x.contains("clear") || x.contains("sunny") => "sunny.png"
      case x if x.contains("mostly sunny")                 => "mostly_sunny.png"
      case x if x.contains("partly cloudy")                => "partly_cloudy.png"
      case x if x.contains("mostly cloudy")                => "mostly_cloudy_day.png"
      case x if x.contains("overcast") || x.contains("cloud") => "cloudy.png"
      case x if x.contains("mist") || x.contains("fog") || x.contains("haze") || x.contains("smoke") => "haze_fog_dust_smoke.png"
      case x if x.contains("drizzle")                      => "drizzle.png"
      case x if x.contains("scattered shower")             => "scattered_showers_day.png"
      case x if x.contains("heavy rain")                   => "heavy_rain.png"
      case x if x.contains("rain") || x.contains("shower") => "showers_rain.png"
      case x if x.contains("isolated") || x.contains("scattered tstorm") => "isolated_scattered_tstorms.png"
      case x if x.contains("thunder") || x.contains("storm") => "strong_tstorms.png"
      case x if x.contains("tornado")                      => "tornado.png"
      case x if x.contains("sleet") || x.contains("hail")  => "sleet_hail.png"
      case x if x.contains("blizzard")                     => "blizzard.png"
      case x if x.contains("blowing snow")                 => "blowing_snow.png"
      case x if x.contains("heavy snow")                   => "heavy_snow.png"
      case x if x.contains("flurries")                     => "flurries.png"
      case x if x.contains("snow shower")                  => "snow_showers_snow.png"
      case x if x.contains("wintry") || x.contains("mix")  => "wintry_mix_rain_snow.png"
      case x if x.contains("snow")                         => "snow_showers_snow.png"
      case _                                               => "partly_cloudy.png" 
    }

    val stream = getClass.getResourceAsStream(s"/icons/$iconName")
    val imgView = new ImageView()
    
    if (stream != null) {
      imgView.image = new Image(stream)
      imgView.fitWidth = size    // Używamy zadanego rozmiaru!
      imgView.fitHeight = size   // Używamy zadanego rozmiaru!
      imgView.preserveRatio = true
    } else {
      println(s"[BŁĄD GRAFIKI] Nie znaleziono pliku: /icons/$iconName")
    }
    
    imgView
  }
}