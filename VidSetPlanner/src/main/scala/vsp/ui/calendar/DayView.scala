package vsp.ui.calendar

import scalafx.scene.layout.{VBox, HBox, Priority, GridPane, ColumnConstraints, RowConstraints}
import scalafx.scene.control.{Label, ScrollPane}
import scalafx.geometry.{Insets, Pos}
import java.time.{LocalDate, LocalTime}
import java.time.format.DateTimeFormatter
import java.util.Locale
import vsp.core.CalendarEventService
import vsp.model.CalendarEvent
import scalafx.Includes._

// Zmieniamy HBox na GridPane dla lepszej kontroli procentowej
class DayView(initialDate: LocalDate) extends GridPane {
  
  padding = Insets(20)
  hgap = 20
  vgrow = Priority.Always
  hgrow = Priority.Always

  // --- KONFIGURACJA KOLUMN (70% - 30%) ---
  val col70 = new ColumnConstraints {
    percentWidth = 70
    hgrow = Priority.Always
  }
  val col30 = new ColumnConstraints {
    percentWidth = 30
    hgrow = Priority.Always
  }
  columnConstraints = Seq(col70, col30)

  // Formatter wymuszający angielski
  private val englishLocale = Locale.ENGLISH
  private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a").withLocale(englishLocale)

  private def getEventsForDay(date: LocalDate): List[CalendarEvent] = {
    CalendarEventService.getAllEvents().filter(_.startTime.toLocalDate == date)
  }

  // --- LEWA STRONA: OŚ CZASU ---
  private val timelineContainer = new VBox {
    spacing = 0
    hgrow = Priority.Always
    style = "-fx-background-color: white;"
  }

  private val timelineScroll = new ScrollPane {
    content = timelineContainer
    fitToWidth = true // Bardzo ważne: zawartość rozciąga się do szerokości ScrollPane
    vgrow = Priority.Always
    style = "-fx-background-color: transparent; -fx-background: white; -fx-border-color: #ddd;"
  }

  // --- PRAWA STRONA: PASEK BOCZNY ---
  private val sidebar = new VBox {
    spacing = 15
    padding = Insets(0, 0, 0, 10)
    hgrow = Priority.Always
  }

  // Dodajemy komponenty do siatki: (komponent, kolumna, wiersz)
  add(timelineScroll, 0, 0)
  add(sidebar, 1, 0)

  def refresh(selectedDate: LocalDate): Unit = {
    timelineContainer.children.clear()
    sidebar.children.clear()

    val dayEvents = getEventsForDay(selectedDate)

    // 1. Oś czasu
    (0 to 23).foreach { hour =>
      val currentHourTime = LocalTime.of(hour, 0)
      val hourEvents = dayEvents.filter(_.startTime.getHour == hour)

      val hourRow = new HBox {
        minHeight = 90 // Trochę wyższe wiersze dla lepszej czytelności
        style = "-fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;"
        
        children = Seq(
          new Label(currentHourTime.format(timeFormatter)) {
            minWidth = 80
            alignment = Pos.TopRight
            padding = Insets(10, 10, 0, 0)
            style = "-fx-text-fill: #999; -fx-font-size: 12px; -fx-font-weight: bold;"
          },
          new VBox {
            hgrow = Priority.Always // Rozciąga karty eventów na całą dostępną szerokość 70%
            padding = Insets(5)
            spacing = 5
            children = hourEvents.map(createEventCard)
          }
        )
      }
      timelineContainer.children.add(hourRow)
    }

    // 2. Pasek boczny
    sidebar.children = Seq(
      new Label(s"Events for ${selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy").withLocale(englishLocale))}") {
        style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        wrapText = true // Zawijanie tekstu, jeśli data jest długa
      },
      new VBox {
        spacing = 10
        children = if (dayEvents.isEmpty) Seq(new Label("No events scheduled") { style = "-fx-text-fill: #95a5a6; -fx-font-style: italic;" })
                   else dayEvents.sortBy(_.startTime).map(createSidebarEventItem)
      }
    )
  }

  private def createEventCard(event: CalendarEvent): VBox = new VBox {
    hgrow = Priority.Always
    style = s"""
      -fx-background-color: rgba(52, 152, 219, 0.1); 
      -fx-border-color: #3498db; 
      -fx-border-width: 0 0 0 5; 
      -fx-padding: 12;
      -fx-background-radius: 5;
    """
    children = Seq(
      new Label(event.title) { 
        style = "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2980b9;" 
      },
      new HBox {
        spacing = 10
        children = Seq(
          new Label(s"🕒 ${event.startTime.toLocalTime}") { style = "-fx-font-size: 11px; -fx-text-fill: #7f8c8d;" },
          new Label(s"📍 ${event.city.name}") { style = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;" }
        )
      }
    )
  }

  private def createSidebarEventItem(event: CalendarEvent): VBox = new VBox {
    padding = Insets(12)
    style = "-fx-background-color: #fdfdfd; -fx-background-radius: 8; -fx-border-color: #ecf0f1; -fx-border-width: 1;"
    children = Seq(
      new Label(event.title) { style = "-fx-font-weight: bold; -fx-text-fill: #34495e;" },
      new Label(s"${event.startTime.toLocalTime} in ${event.city.name}") { 
        style = "-fx-font-size: 11px; -fx-text-fill: #bdc3c7;" 
      }
    )
  }

  refresh(initialDate)
}