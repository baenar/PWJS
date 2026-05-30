package vsp.ui

import scalafx.scene.layout.{BorderPane, HBox, Priority, Region, VBox}
import scalafx.scene.control.{Button, Label}
import scalafx.geometry.{Insets, Pos}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import vsp.ui.calendar.{MonthGridView, DayView, AgendaView}
import vsp.ui.dialogs.AddEventDialog
import scalafx.Includes._

class MainView extends BorderPane {
  
  private var currentActiveDate: LocalDate = LocalDate.now()
  private var viewMode: String = "MONTH"

  // --- WIDOKI ---
  private lazy val monthView = new MonthGridView(currentActiveDate, date => {
    currentActiveDate = date
    switchToDayView()
  })
  
  private lazy val dayView = new DayView(currentActiveDate)
  private lazy val agendaView = new AgendaView(() => refreshCurrentView())

  // --- ELEMENTY NAGŁÓWKA ---
  
  private val monthLabel = new Label {
    style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-min-width: 200px;"
    alignment = Pos.Center
  }

  // Wyciągamy przyciski wyżej, aby mieć do nich dostęp z innych funkcji
  private val btnPrev = new Button("<") {
    style = "-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-font-size: 18px; -fx-cursor: hand;"
    onAction = _ => {
      if (viewMode == "MONTH") currentActiveDate = currentActiveDate.minusMonths(1)
      else if (viewMode == "DAY") currentActiveDate = currentActiveDate.minusDays(1)
      refreshCurrentView()
    }
  }

  private val btnNext = new Button(">") {
    style = "-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-font-size: 18px; -fx-cursor: hand;"
    onAction = _ => {
      if (viewMode == "MONTH") currentActiveDate = currentActiveDate.plusMonths(1)
      else if (viewMode == "DAY") currentActiveDate = currentActiveDate.plusDays(1)
      refreshCurrentView()
    }
  }

  private val navBox = new HBox {
    alignment = Pos.CenterLeft
    spacing = 15
    children = Seq(btnPrev, monthLabel, btnNext)
  }

  // --- FUNKCJA AKTUALIZUJĄCA UI NAGŁÓWKA ---
  private def updateDateDisplay(): Unit = {
    val englishLocale = java.util.Locale.ENGLISH
    
    // Sprawdzamy, czy pokazywać strzałki
    val showArrows = (viewMode != "AGENDA")
    
    // Ukrywamy/Pokazujemy strzałki i zwijamy/rozwijamy ich przestrzeń
    btnPrev.visible = showArrows
    btnPrev.managed = showArrows
    btnNext.visible = showArrows
    btnNext.managed = showArrows

    viewMode match {
      case "MONTH" => 
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(englishLocale)
        monthLabel.text = currentActiveDate.format(formatter).capitalize
      case "DAY" => 
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy").withLocale(englishLocale)
        monthLabel.text = currentActiveDate.format(formatter).capitalize
      case "AGENDA" =>
        monthLabel.text = "Upcoming Events"
    }
  }

  private val actionBox = new HBox {
    alignment = Pos.CenterRight
    spacing = 15
    
    val btnMonth = new Button("M") {
      style = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"
      onAction = _ => switchToMonthView()
    }
    val btnDay = new Button("D") {
      style = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"
      onAction = _ => switchToDayView()
    }
    val btnAgenda = new Button("A") {
      style = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"
      onAction = _ => switchToAgendaView()
    }

    val plusBtn = new Button("+") {
      style = """
        -fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 20px; 
        -fx-font-weight: bold; -fx-background-radius: 50; -fx-min-width: 45px; 
        -fx-min-height: 45px; -fx-cursor: hand;
      """
      onAction = _ => {
        val dateToPlan = if (viewMode == "MONTH") monthView.selectedDate else currentActiveDate
        val dialog = new AddEventDialog(dateToPlan, vsp.model.City(1, "Warszawa", "PL"))
        dialog.showAndWait() match {
          case Some(ev: vsp.model.CalendarEvent) => 
            vsp.core.CalendarEventService.addEvent(ev)
            refreshCurrentView()
          case _ =>
        }
      }
    }
    
    children = Seq(btnMonth, btnDay, btnAgenda, plusBtn)
  }

  private val headerToolbar = new HBox {
    padding = Insets(20)
    style = "-fx-background-color: #2c3e50;" 
    alignment = Pos.Center
    
    val spacer = new Region { hgrow = Priority.Always }
    children = Seq(navBox, spacer, actionBox)
  }

  top = headerToolbar
  center = monthView
  updateDateDisplay()

  // --- LOGIKA ZMIANY WIDOKÓW ---

  private def switchToMonthView(): Unit = {
    viewMode = "MONTH"
    monthView.selectedDate = currentActiveDate
    updateDateDisplay()
    monthView.refresh(currentActiveDate)
    center = monthView
  }

  private def switchToDayView(): Unit = {
    if (viewMode == "MONTH") currentActiveDate = monthView.selectedDate
    viewMode = "DAY"
    dayView.currentViewDate = currentActiveDate
    updateDateDisplay()
    dayView.refresh(currentActiveDate)
    center = dayView
  }

  private def switchToAgendaView(): Unit = {
    viewMode = "AGENDA"
    updateDateDisplay()
    agendaView.refresh()
    center = agendaView
  }

  private def refreshCurrentView(): Unit = {
    updateDateDisplay()
    viewMode match {
      case "MONTH" => monthView.refresh(currentActiveDate)
      case "DAY" => dayView.refresh(currentActiveDate)
      case "AGENDA" => agendaView.refresh()
    }
  }
}