package vsp.persistence

import org.flywaydb.core.Flyway

object FlywayMigrator {

  def migrate(): Unit = {
    val flyway = Flyway.configure()
      .dataSource(DbConfig.url, null, null)
      .load()
    flyway.migrate()
  }
}
