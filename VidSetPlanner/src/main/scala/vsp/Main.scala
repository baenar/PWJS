package vsp

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import vsp.ui.MainView
import vsp.persistence.FlywayMigrator

object Main extends JFXApp3 {

  override def start(): Unit = {
    // 1. Migracja bazy danych
    FlywayMigrator.migrate()

    // 2. Uruchomienie interfejsu
    stage = new JFXApp3.PrimaryStage {
      title = "VidSet Planner"
      scene = new Scene(1200, 800) {
        root = new MainView() 
      }
    }
  }
}