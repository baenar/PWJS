package vsp.persistence.slickdb

import slick.jdbc.SQLiteProfile.api._
import vsp.persistence.DbConfig

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try

object SlickDb {
  private val sqliteDriverClass = "org.sqlite.JDBC"
  private val timeout = 10.seconds

  def database: Database =
    Database.forURL(
      url = DbConfig.url,
      driver = sqliteDriverClass
    )

  /**
   * Small blocking helper for this desktop/student project.
   *
   * Slick is asynchronous by default, but the existing project uses simple
   * synchronous repository methods. This wrapper keeps the new Slick repositories
   * easy to compare with the current JDBC repositories.
   */
  def run[T](action: DBIO[T]): Try[T] = {
    val db = database
    val result = Try(Await.result(db.run(action), timeout))
    db.close()
    result
  }
}
