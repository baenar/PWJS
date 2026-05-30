package vsp.ui.dialogs

import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.Insets
import scalafx.scene.input.MouseEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import vsp.model.CalendarEvent
import vsp.core.CalendarEventService // Potrzebne do pobierania świeżych danych przy odświeżaniu

class DailyEventsDialog(date: LocalDate, onCalendarRefresh: () => Unit) extends Dialog[Unit] {
  title = "Daily Events"
  dialogPane().buttonTypes = Seq(ButtonType.Close)

  private val englishLocale = Locale.ENGLISH
  private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy").withLocale(englishLocale)

  private val header = new Label(s"Events for ${date.format(dateFormatter)}") {
    style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 15 0;"
  }

  private val listLayout = new VBox(10) {
    padding = Insets(5, 15, 5, 5)
  }

  private val scrollPane = new ScrollPane {
    content = listLayout
    fitToWidth = true
    prefHeight = 300
    prefWidth = 350
    style = "-fx-background-color: transparent; -fx-background: white;"
  }

  dialogPane().content = new VBox {
    padding = Insets(20)
    children = Seq(header, scrollPane)
  }

  // --- NOWA METODA ODŚWIEŻAJĄCA LISTĘ ---
  def refreshList(): Unit = {
    listLayout.children.clear()

    // Pobieramy aktualne wydarzenia z bazy dla tego dnia (żeby widzieć zmiany)
    val freshEvents = CalendarEventService.getAllEvents().filter(_.startTime.toLocalDate == date)

    freshEvents.sortBy(_.startTime).foreach { event =>
      val card = new VBox {
        padding = Insets(12)
        style = "-fx-background-color: #fdfdfd; -fx-background-radius: 8; -fx-border-color: #ecf0f1; -fx-border-width: 1; -fx-cursor: hand;"
        
        onMouseClicked = (e: MouseEvent) => {
          // ŁAŃCUSZEK ODŚWIEŻANIA:
          // Przekazujemy funkcję, która najpierw odświeży tę listę, 
          // a potem wywoła odświeżenie głównego kalendarza pod spodem!
          val detailsDialog = new EventDetailsDialog(event, () => {
            refreshList()       // 1. Odśwież listę w tym oknie
            onCalendarRefresh()     // 2. Odśwież kalendarz w tle
          })
          detailsDialog.showAndWait()
        }

        children = Seq(
          new Label(event.title) { style = "-fx-font-weight: bold; -fx-text-fill: #34495e;" },
          new Label(s"${event.startTime.toLocalTime} - ${event.endTime.toLocalTime} in ${event.city.name}") { 
            style = "-fx-font-size: 11px; -fx-text-fill: #bdc3c7;" 
          }
        )
      }
      listLayout.children.add(card)
    }
  }

  // Pierwsze uruchomienie przy otwarciu okna
  refreshList()
}