package vsp.persistence

object DbConfig {
  /**
    * Database URL used by repositories and Flyway.
    *
    * Defaults to the normal local application database, but tests can override it with:
    *   -Dvsp.db.url=jdbc:sqlite:target/test-vidsetplanner.db
    *
    * This intentionally reads the value dynamically, so tests may set the property
    * before running migrations/repository calls without touching production code paths.
    */
  def url: String =
    Option(System.getProperty("vsp.db.url"))
      .orElse(Option(System.getenv("VSP_DB_URL")))
      .getOrElse("jdbc:sqlite:vidsetplanner.db")
}
