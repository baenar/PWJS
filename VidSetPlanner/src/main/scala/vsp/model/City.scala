package vsp.model

case class City(
  id: Int,
  name: String,
  country: String,
  latitude: Double,
  longitude: Double
)

object City {
  def create(name: String, country: String, latitude: Double, longitude: Double): City = {
    City(0, name, country, latitude, longitude)
  }
}
