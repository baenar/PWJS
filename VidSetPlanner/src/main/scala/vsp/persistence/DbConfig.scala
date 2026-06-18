package vsp.persistence

object DbConfig {
  /**
   * Default application database.
   *
   * Tests and local experiments can override it without touching production data:
   *   mvn test -Dvsp.db.url=jdbc:sqlite:target/test-dbs/test.db
   *
   * Environment variable fallback is useful in Docker/CI:
   *   VSP_DB_URL=jdbc:sqlite:/tmp/vidsetplanner.db
   */
  def url: String =
    Option(System.getProperty("vsp.db.url"))
      .filter(_.trim.nonEmpty)
      .orElse(Option(System.getenv("VSP_DB_URL")).filter(_.trim.nonEmpty))
      .getOrElse("jdbc:sqlite:vidsetplanner.db")
}
