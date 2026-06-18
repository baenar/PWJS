package vsp

import munit.FunSuite
import vsp.persistence.FlywayMigrator

import java.nio.file.{Files, Path, Paths}

/**
  * Base suite for repository/service tests that need SQLite.
  *
  * Each test gets a fresh database file under target/test-dbs, so tests do not
  * touch the normal vidsetplanner.db used by the application/demo.
  */
abstract class TestDatabaseSuite extends FunSuite {

  private val dbDir: Path = Paths.get("target", "test-dbs")

  private def dbPath(testName: String): Path = {
    val safeName = testName.replaceAll("[^a-zA-Z0-9._-]", "_")
    dbDir.resolve(s"${getClass.getSimpleName}-$safeName.db")
  }

  private def deleteIfExists(path: Path): Unit = {
    Files.deleteIfExists(path)
    Files.deleteIfExists(Paths.get(path.toString + "-wal"))
    Files.deleteIfExists(Paths.get(path.toString + "-shm"))
  }

  override def beforeEach(context: BeforeEach): Unit = {
    Files.createDirectories(dbDir)
    val path = dbPath(context.test.name)
    deleteIfExists(path)

    val dbUrl = s"jdbc:sqlite:${path.toAbsolutePath}"
    System.setProperty("vsp.db.url", dbUrl)
    FlywayMigrator.migrate(dbUrl)
  }

  override def afterEach(context: AfterEach): Unit = {
    System.clearProperty("vsp.db.url")
  }
}
