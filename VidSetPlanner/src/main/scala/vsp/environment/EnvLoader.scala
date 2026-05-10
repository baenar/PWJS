package vsp.environment

import scala.io.Source
import scala.util.Try

object EnvLoader {

  private lazy val vars: Map[String, String] = {
    Try {
      val lines = Source.fromFile(".env").getLines().toList
      lines
        .map(_.trim)
        .filterNot(line => line.isEmpty || line.startsWith("#"))
        .flatMap { line =>
          val idx = line.indexOf('=')
          if (idx > 0) {
            Some(line.substring(0, idx).trim -> line.substring(idx + 1).trim)
          } else {
            None
          }
        }
        .toMap
    }.getOrElse(Map.empty)
  }

  def get(key: String): String = {
    vars.getOrElse(key, System.getenv(key))
  }
}
