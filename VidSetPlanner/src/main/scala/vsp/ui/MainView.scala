package vsp.ui

import scalafx.scene.layout.{BorderPane, HBox, Priority, Region, VBox}
import scalafx.scene.control.{Button, Label}
import scalafx.geometry.{Insets, Pos}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import vsp.ui.calendar.{MonthGridView, DayView}
import vsp.ui.dialogs.AddEventDialog
import scalafx.Includes._
import scalafx.scene.paint.Color

class MainView extends BorderPane {
  
  private var currentActiveDate: LocalDate = LocalDate.now()
  private var isMonthMode: Boolean = true

  // --- WIDOKI ---
  private lazy val monthView = new MonthGridView(currentActiveDate, date => {
    currentActiveDate = date
    switchToDayView()
  })
  
  private lazy val dayView = new DayView(currentActiveDate)

  // --- ELEMENTY NAGŁÓWKA ---
  
  // 1. Nazwa miesiąca i nawigacja (Lewa strona)
  private val monthLabel = new Label {
    style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white; -fx-min-width: 200px;"
    alignment = Pos.Center
  }

  private def updateDateDisplay(): Unit = {
    // Dodajemy import na początku pliku: import java.util.Locale
    val englishLocale = java.util.Locale.ENGLISH
    
    val formatter = if (isMonthMode) 
      DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(englishLocale) 
    else 
      DateTimeFormatter.ofPattern("dd MMMM yyyy").withLocale(englishLocale)
      
    monthLabel.text = currentActiveDate.format(formatter).capitalize
  }
  private val navBox = new HBox {
    alignment = Pos.CenterLeft
    spacing = 15
    val btnPrev = new Button("<") {
      style = "-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-font-size: 18px; -fx-cursor: hand;"
      onAction = _ => {
        currentActiveDate = if (isMonthMode) currentActiveDate.minusMonths(1) else currentActiveDate.minusDays(1)
        refreshCurrentView()
      }
    }
    val btnNext = new Button(">") {
      style = "-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-font-size: 18px; -fx-cursor: hand;"
      onAction = _ => {
        currentActiveDate = if (isMonthMode) currentActiveDate.plusMonths(1) else currentActiveDate.plusDays(1)
        refreshCurrentView()
      }
    }
    children = Seq(btnPrev, monthLabel, btnNext)
  }

  // 2. Przełącznik widoków i Plus (Prawa strona)
  private val actionBox = new HBox {
    alignment = Pos.CenterRight
    spacing = 15
    
    // Małe przyciski przełączania
    val btnMonth = new Button("M") {
      style = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"
      onAction = _ => switchToMonthView()
    }
    val btnDay = new Button("D") {
      style = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"
      onAction = _ => switchToDayView()
    }

    // Okrągły przycisk PLUS
    val plusBtn = new Button("+") {
      style = """
        -fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 20px; 
        -fx-font-weight: bold; -fx-background-radius: 50; -fx-min-width: 45px; 
        -fx-min-height: 45px; -fx-cursor: hand;
      """
      onAction = _ => {
        val dateToPlan = if (isMonthMode) monthView.selectedDate else currentActiveDate

        val dialog = new AddEventDialog(dateToPlan, vsp.model.City(1, "Warszawa", "PL"))
        dialog.showAndWait() match {
          case Some(ev: vsp.model.CalendarEvent) => 
            vsp.core.CalendarEventService.addEvent(ev)
            refreshCurrentView()
          case _ =>
        }
      }
    }
    children = Seq(btnMonth, btnDay, plusBtn)
  }

  // --- SKŁADANIE PASKA ---
  private val headerToolbar = new HBox {
    padding = Insets(20)
    style = "-fx-background-color: #2c3e50;" // Ciemny granat tła
    alignment = Pos.Center
    
    val spacer = new Region { hgrow = Priority.Always }
    
    children = Seq(navBox, spacer, actionBox)
  }

  top = headerToolbar
  center = monthView
  updateDateDisplay()

  // --- LOGIKA ---

  private def switchToMonthView(): Unit = {
    isMonthMode = true
    updateDateDisplay()
    monthView.refresh(currentActiveDate)
    center = monthView
  }

  private def switchToDayView(): Unit = {
    currentActiveDate = monthView.selectedDate

    isMonthMode = false
    updateDateDisplay()
    dayView.refresh(currentActiveDate)
    center = dayView
  }

  private def refreshCurrentView(): Unit = {
    updateDateDisplay()
    if (isMonthMode) monthView.refresh(currentActiveDate)
    else dayView.refresh(currentActiveDate)
  }
}