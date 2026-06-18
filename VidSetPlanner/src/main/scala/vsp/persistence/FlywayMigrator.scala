package vsp.persistence

import org.flywaydb.core.Flyway

object FlywayMigrator {

  def migrate(): Unit = migrate(DbConfig.url)

  def migrate(dbUrl: String): Unit = {
    val flyway = Flyway.configure()
      .dataSource(dbUrl, null, null)
      .load()

    flyway.migrate()
  }
}
