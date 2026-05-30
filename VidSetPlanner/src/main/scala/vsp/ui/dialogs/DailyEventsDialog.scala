package vsp.ui.dialogs

import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.input.MouseEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import vsp.model.CalendarEvent
import vsp.core.CalendarEventService 

class DailyEventsDialog(date: LocalDate, onCalendarRefresh: () => Unit) extends Dialog[Unit] {
  
  title = "Daily Events"
  headerText = "" // Usuwamy domyślny nagłówek dla estetyki

  // --- PRZYCISK ZAMYKANIA ---
  val closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CancelClose)
  dialogPane().buttonTypes = Seq(closeButtonType)

  private val closeBtn = dialogPane().lookupButton(closeButtonType).asInstanceOf[javafx.scene.control.Button]
  closeBtn.style = "-fx-background-color: white; -fx-text-fill: #374151; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;"

  // Styling okna głównego
  dialogPane().style = "-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12;"

  private val englishLocale = Locale.ENGLISH
  private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy").withLocale(englishLocale)

  // --- NAGŁÓWEK ---
  private val header = new Label(date.format(dateFormatter)) {
    style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827; -fx-padding: 0 0 15 0;"
  }

  // --- KONTENER NA LISTĘ ---
  private val listLayout = new VBox(12) {
    padding = Insets(5, 15, 15, 5) // Lekki padding, by scrollbar i cienie miały miejsce
  }

  private val scrollPane = new ScrollPane {
    content = listLayout
    fitToWidth = true
    prefHeight = 350
    style = "-fx-background-color: transparent; -fx-background: white;" // Usuwa szare tło domyślnego scrolla
  }

  dialogPane().content = new VBox {
    padding = Insets(20)
    prefWidth = 420
    children = Seq(header, scrollPane)
  }

  // --- LOGIKA RYSOWANIA KART ---
  def refreshList(): Unit = {
    listLayout.children.clear()

    // Pobieramy aktualne wydarzenia z bazy
    val freshEvents = CalendarEventService.getAllEvents().filter(_.startTime.toLocalDate == date)

    // Stan pusty (Empty State) - np. gdy użytkownik usunie wszystkie wydarzenia z listy
    if (freshEvents.isEmpty) {
      listLayout.children = Seq(
        new Label("No events remaining for this day.") {
          style = "-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-padding: 30 0;"
          alignment = Pos.Center
          maxWidth = Double.MaxValue
        }
      )
      return
    }

    // Rysowanie poszczególnych kart
    freshEvents.sortBy(_.startTime).foreach { event =>
      val card = new VBox(6) {
        padding = Insets(14, 16, 14, 16)
        
        // Zwykły wygląd karty
        val defaultStyle = """
          -fx-background-color: #ffffff; 
          -fx-background-radius: 8; 
          -fx-border-color: #e5e7eb; 
          -fx-border-radius: 8;
          -fx-border-width: 1.5;
          -fx-cursor: hand;
        """
        
        // Wygląd po najechaniu myszką (Niebieska ramka)
        val hoverStyle = defaultStyle.replace("-fx-border-color: #e5e7eb;", "-fx-border-color: #3b82f6;")

        style = defaultStyle
        
        onMouseEntered = _ => style = hoverStyle
        onMouseExited = _ => style = defaultStyle
        
        onMouseClicked = (e: MouseEvent) => {
          // Otwieramy estetyczny EventDetailsDialog
          val detailsDialog = new EventDetailsDialog(event, () => {
            refreshList()       // Odśwież listę, jeśli coś się zmieniło
            onCalendarRefresh() // Odśwież kalendarz pod spodem
          })
          detailsDialog.showAndWait()
        }

        children = Seq(
          new Label(event.title) { 
            style = "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;" 
          },
          new HBox(15) {
            children = Seq(
              new Label(s"🕒 ${event.startTime.toLocalTime} - ${event.endTime.toLocalTime}") { 
                style = "-fx-font-size: 12px; -fx-text-fill: #6b7280;" 
              },
              new Label(s"📍 ${event.city.name}") { 
                style = "-fx-font-size: 12px; -fx-text-fill: #6b7280;" 
              }
            )
          }
        )
      }
      listLayout.children.add(card)
    }
  }

  // Pierwsze uruchomienie przy otwarciu okna
  refreshList()
}