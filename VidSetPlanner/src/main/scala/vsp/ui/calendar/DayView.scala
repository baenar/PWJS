package vsp.ui.calendar

import scalafx.scene.layout.{VBox, HBox, Priority, GridPane, ColumnConstraints, Pane}
import scalafx.scene.control.{Label, ScrollPane}
import scalafx.scene.shape.Line
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.input.MouseEvent
import java.time.{LocalDate, LocalTime, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.util.Locale
import scala.collection.mutable.ListBuffer
import vsp.core.CalendarEventService
import vsp.model.CalendarEvent
import vsp.ui.dialogs.EventDetailsDialog
import scalafx.Includes._
import scalafx.scene.image.{Image, ImageView}
import scalafx.geometry.{Pos, Insets}
import scalafx.scene.layout.HBox
import vsp.ui.util.WeatherIconUtil

class DayView(initialDate: LocalDate) extends GridPane {
  
  // Zmienna musi być publiczna, aby MainView miał do niej dostęp
  var currentViewDate: LocalDate = initialDate

  // --- STAŁE DO OBLICZEŃ WIZUALNYCH ---
  val HOUR_HEIGHT = 60.0 // 1 godzina = 60 pikseli
  val TIME_COL_W = 75.0  // Szerokość lewej kolumny z godzinami

  padding = Insets(20)
  hgap = 20
  vgrow = Priority.Always
  hgrow = Priority.Always

  val col70 = new ColumnConstraints { percentWidth = 70; hgrow = Priority.Always }
  val col30 = new ColumnConstraints { percentWidth = 30; hgrow = Priority.Always }
  columnConstraints = Seq(col70, col30)

  private val englishLocale = Locale.ENGLISH
  private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a").withLocale(englishLocale)

  // Pobieranie wydarzeń z uwzględnieniem tych przechodzących przez północ
  private def getEventsForDay(date: LocalDate): List[CalendarEvent] = {
    CalendarEventService.getAllEvents().filter { event =>
      val startDay = event.startTime.toLocalDate
      val endDay = event.endTime.toLocalDate
      startDay == date || endDay == date || (startDay.isBefore(date) && endDay.isAfter(date))
    }
  }

  private val timelinePane = new Pane {
    prefHeight = 24 * HOUR_HEIGHT 
    style = "-fx-background-color: white;"
  }

  private val timelineScroll = new ScrollPane {
    content = timelinePane
    fitToWidth = true
    vgrow = Priority.Always
    style = "-fx-background-color: transparent; -fx-background: white; -fx-border-color: #ddd;"
  }

  private val sidebar = new VBox {
    spacing = 15
    padding = Insets(0, 0, 0, 10)
    hgrow = Priority.Always
  }

  add(timelineScroll, 0, 0)
  add(sidebar, 1, 0)

  def refresh(selectedDate: LocalDate): Unit = {
    currentViewDate = selectedDate
    timelinePane.children.clear()
    sidebar.children.clear()

    val dayEvents = getEventsForDay(selectedDate)

    // 1. Rysowanie siatki
    (0 to 23).foreach { hour =>
      val yPosition = hour * HOUR_HEIGHT
      
      val timeLabel = new Label(LocalTime.of(hour, 0).format(timeFormatter)) {
        layoutX = 0
        layoutY = yPosition - 8
        prefWidth = TIME_COL_W - 10
        alignment = Pos.TopRight
        style = "-fx-text-fill: #999; -fx-font-size: 11px; -fx-font-weight: bold;"
      }
      
      val line = new Line {
        startX = TIME_COL_W
        startY = yPosition
        endY = yPosition
        stroke = scalafx.scene.paint.Color.web("#ecf0f1")
      }
      line.endX <== timelinePane.widthProperty() 

      timelinePane.children.addAll(timeLabel, line)
    }

    // 2. Klastrowanie nakładających się wydarzeń
    val sortedEvents = dayEvents.sortBy(e => (e.startTime, -e.endTime.getHour * 60 - e.endTime.getMinute))
    val clusters = ListBuffer[ListBuffer[CalendarEvent]]()
    var currentCluster = ListBuffer[CalendarEvent]()
    var clusterEnd = LocalDateTime.MIN

    for (ev <- sortedEvents) {
      if (currentCluster.isEmpty) {
        currentCluster += ev
        clusterEnd = ev.endTime
      } else if (ev.startTime.isBefore(clusterEnd)) {
        currentCluster += ev
        if (ev.endTime.isAfter(clusterEnd)) clusterEnd = ev.endTime
      } else {
        clusters += currentCluster
        currentCluster = ListBuffer(ev)
        clusterEnd = ev.endTime
      }
    }
    if (currentCluster.nonEmpty) clusters += currentCluster

    // 3. Pozycjonowanie i rozmiar wydarzeń
    for (cluster <- clusters) {
      val columns = ListBuffer[ListBuffer[CalendarEvent]]()
      
      for (ev <- cluster) {
        columns.find(col => !col.exists(e => e.startTime.isBefore(ev.endTime) && e.endTime.isAfter(ev.startTime))) match {
          case Some(col) => col += ev
          case None => columns += ListBuffer(ev)
        }
      }

      val numCols = columns.size

      for ((col, colIndex) <- columns.zipWithIndex) {
        for (ev <- col) {
          val card = createEventCard(ev)

          val startMinutes = if (ev.startTime.toLocalDate.isBefore(currentViewDate)) 0 
                             else ev.startTime.getHour * 60 + ev.startTime.getMinute

          val endMinutes = if (ev.endTime.toLocalDate.isAfter(currentViewDate)) 24 * 60 
                           else ev.endTime.getHour * 60 + ev.endTime.getMinute

          val duration = Math.max(endMinutes - startMinutes, 15)

          card.layoutY = startMinutes * (HOUR_HEIGHT / 60.0)
          card.prefHeight = duration * (HOUR_HEIGHT / 60.0)
          card.layoutX <== timelinePane.widthProperty().subtract(TIME_COL_W).divide(numCols).multiply(colIndex).add(TIME_COL_W)
          card.prefWidth <== timelinePane.widthProperty().subtract(TIME_COL_W).divide(numCols).subtract(4)

          timelinePane.children.add(card)
        }
      }
    }

    // 4. Pasek boczny
    sidebar.children = Seq(
      new Label(s"Events for ${selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy").withLocale(englishLocale))}") {
        style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        wrapText = true
      },
      new VBox {
        spacing = 10
        children = if (dayEvents.isEmpty) Seq(new Label("No events scheduled") { style = "-fx-text-fill: #95a5a6; -fx-font-style: italic;" })
                   else dayEvents.sortBy(_.startTime).map(createSidebarEventItem)
      }
    )
  }

  private def createEventCard(event: CalendarEvent): VBox = new VBox {
    style = s"""
      -fx-background-color: rgba(52, 152, 219, 0.15); 
      -fx-border-color: #3498db; 
      -fx-border-width: 0 0 0 4; 
      -fx-padding: 5 10 5 10;
      -fx-background-radius: 4;
      -fx-cursor: hand;
    """
    
    onMouseClicked = (e: MouseEvent) => {
      val dialog = new EventDetailsDialog(event, () => refresh(currentViewDate))
      dialog.showAndWait()
      refresh(currentViewDate)
    }

    children = Seq(
      new Label(event.title) { style = "-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #2980b9;" },
      new Label(s"${event.startTime.toLocalTime} - ${event.endTime.toLocalTime}") { style = "-fx-font-size: 10px; -fx-text-fill: #7f8c8d;" }
    )
  }

  private def createSidebarEventItem(event: CalendarEvent): VBox = new VBox {
    padding = Insets(12)
    style = "-fx-background-color: #fdfdfd; -fx-background-radius: 8; -fx-border-color: #ecf0f1; -fx-border-width: 1; -fx-cursor: hand;"
    
    onMouseClicked = (e: MouseEvent) => {
      val dialog = new EventDetailsDialog(event, () => refresh(currentViewDate))
      dialog.showAndWait()
      refresh(currentViewDate)
    }

    // 1. Sprawdzamy i budujemy element pogodowy
    val weatherInfo: Seq[scalafx.scene.Node] = event.weather match {
      case Some(w) =>
        val iconView = WeatherIconUtil.getWeatherIcon(w, 24.0) 
        val tempString = event.temperature.map(t => f"$t%.1f°C").getOrElse("")
        
        import scalafx.scene.control.Tooltip
        val weatherTooltip = new Tooltip {
          text = s"Weather: $w"
          style = "-fx-font-size: 13px; -fx-background-color: rgba(17, 24, 39, 0.9);"
        }
        
        // NAPRAWA: Opakowujemy sam obrazek w Label, żeby móc przypiąć do niego Tooltip
        val iconLabel = new Label {
          graphic = iconView
          tooltip = weatherTooltip
        }

        Seq(
          new Label(" ") { style = "-fx-padding: 0 2 0 2;" }, // Mały odstęp
          iconLabel, // <-- Używamy naszego nowego opakowanego obrazka
          new Label(tempString) { 
            style = "-fx-font-size: 11px; -fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-padding: 0 0 0 4;" 
            tooltip = weatherTooltip
          }
        )
      case None => 
        Seq.empty // Brak pogody
    }

    // 2. Składamy wiersz ze szczegółami (czas, miejsce i doklejona pogoda)
    val detailsRow = new HBox {
      alignment = Pos.CenterLeft
      children = Seq(
        new Label(s"${event.startTime.toLocalTime} - ${event.endTime.toLocalTime} in ${event.city.name}") { 
          style = "-fx-font-size: 11px; -fx-text-fill: #bdc3c7;" 
        }
      ) ++ weatherInfo
    }

    // 3. Dodajemy wszystko do kafelka
    children = Seq(
      new Label(event.title) { style = "-fx-font-weight: bold; -fx-text-fill: #34495e;" },
      new scalafx.scene.layout.Region { prefHeight = 4 }, // Lekki odstęp między tytułem a szczegółami
      detailsRow
    )
  }

  refresh(initialDate)
}