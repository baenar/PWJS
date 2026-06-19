package vsp

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import vsp.ui.MainView
import vsp.persistence.FlywayMigrator
import vsp.api.CalendarSync

object Main extends JFXApp3 {

  override def start(): Unit = {
    // 1. Migracja bazy danych
    FlywayMigrator.migrate()

    // 2. Aktualizacja danych google calendar (synchronizacja w obie strony)
    CalendarSync.syncOnStartup()

    // 3. Uruchomienie interfejsu
    stage = new JFXApp3.PrimaryStage {
      title = "VidSet Planner"
      scene = new Scene(1200, 800) {
        root = new MainView() 
      }
    }
  }
}