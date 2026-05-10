package vsp.ui.calendar

import scalafx.scene.layout.{GridPane, Priority, VBox, HBox, ColumnConstraints, RowConstraints}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.input.MouseEvent
import scalafx.geometry.{Insets, Pos}
import java.time.LocalDate
import vsp.util.DateUtils
import scalafx.Includes._
import vsp.ui.dialogs.AddEventDialog
import vsp.persistence.EventRepository

class MonthGridView(initialDate: LocalDate, onDateSelected: LocalDate => Unit) extends VBox {
  
  spacing = 5
  padding = Insets(10)
  vgrow = Priority.Always

  private var selectedDate: LocalDate = LocalDate.now()

  // 1. Toolbar
  /*private val toolbar = new HBox {
    alignment = Pos.CenterRight
    padding = Insets(0, 0, 10, 0)
    val plusBtn = new Button("+") {
      style = """
        -fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 18px; 
        -fx-font-weight: bold; -fx-background-radius: 50; -fx-min-width: 40px; 
        -fx-min-height: 40px; -fx-cursor: hand;
      """
      onAction = _ => {
        val dialog = new AddEventDialog(selectedDate, vsp.model.City(1, "Warszawa", "PL"))
        val result = dialog.showAndWait()
        
        result match {
          case Some(newEvent: vsp.model.CalendarEvent) =>
            EventRepository.save(newEvent)
            refresh(selectedDate) // Odświeżamy po dodaniu przyciskiem Plus
          case _ => 
        }
      }
    }
    children = Seq(plusBtn)
  }*/

  // 2. Nagłówki dni tygodnia
  private val weekdayHeader = new GridPane {
    hgap = 5
    columnConstraints = (1 to 7).map(_ => new ColumnConstraints {
      percentWidth = 100.0 / 7
      hgrow = Priority.Always
    })
    val dayNames = Seq("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    dayNames.zipWithIndex.foreach { case (name, i) =>
      add(new Label(name) {
        maxWidth = Double.MaxValue
        alignment = Pos.Center
        style = "-fx-font-weight: bold; -fx-text-fill: #7f8c8d; -fx-padding: 5;"
      }, i, 0)
    }
  }

  // 3. Główna siatka
  private val calendarGrid = new GridPane {
    hgap = 5; vgap = 5
    hgrow = Priority.Always; vgrow = Priority.Always
    columnConstraints = (1 to 7).map(_ => new ColumnConstraints {
      percentWidth = 100.0 / 7
      hgrow = Priority.Always
    })
  }

  children = Seq(/*toolbar, */weekdayHeader, calendarGrid)

  def refresh(currentMonth: LocalDate): Unit = {
    calendarGrid.children.clear()
    calendarGrid.rowConstraints.clear()

    val allEventsFromDb = EventRepository.findAll()

    val days = DateUtils.getDaysForMonthGrid(currentMonth)
    val rowCount = Math.ceil(days.length.toDouble / 7).toInt
    
    calendarGrid.rowConstraints = (1 to rowCount).map(_ => new RowConstraints {
      percentHeight = 100.0 / rowCount
      vgrow = Priority.Always
      fillHeight = true
    })

    days.zipWithIndex.foreach { case (day, index) =>
      val column = index % 7
      val row = index / 7

      val dayCell = new VBox {
        maxWidth = Double.MaxValue
        maxHeight = Double.MaxValue
        spacing = 2
        padding = Insets(2)

        minHeight = 0
        
        val isSelected = day == selectedDate
        val isToday = day == LocalDate.now()
        val isCurrentMonth = day.getMonth == currentMonth.getMonth

        style = determineStyle(isSelected, isToday, isCurrentMonth, isHovered = false)

        val dayEvents = allEventsFromDb.filter(_.startTime.toLocalDate == day)

        val MAX_VISIBLE_EVENTS = 2
        val visibleEvents = dayEvents.take(MAX_VISIBLE_EVENTS)
        val hiddenCount = dayEvents.size - MAX_VISIBLE_EVENTS

        val eventLabels = visibleEvents.map { event =>
          new Label(event.title) {
            maxWidth = Double.MaxValue
            style = """
              -fx-background-color: #d1ecf1; 
              -fx-text-fill: #0c5460; 
              -fx-font-size: 10px; 
              -fx-padding: 1 4 1 4; 
              -fx-background-radius: 3;
            """
            ellipsisString = "..."
          }
        }

        val moreLabel = if (hiddenCount > 0) {
          Seq(new Label(s"+$hiddenCount more") {
            maxWidth = Double.MaxValue
            style = s"""
              -fx-text-fill: ${if (isSelected) "white" else "#7f8c8d"};
              -fx-font-size: 9px; 
              -fx-font-weight: bold;
              -fx-padding: 1 4 1 4;
            """
          })
        } else Nil

        onMouseEntered = _ => {
          if (!isSelected) style = determineStyle(isSelected, isToday, isCurrentMonth, isHovered = true)
        }
        onMouseExited = _ => {
          if (!isSelected) style = determineStyle(isSelected, isToday, isCurrentMonth, isHovered = false)
        }

        onMouseClicked = (e: MouseEvent) => {
          if (e.clickCount == 2) {
            // 1. Obsługa dwukliku -> Widok dnia
            onDateSelected(day)
          } else {
            // 2. Obsługa pojedynczego kliknięcia -> Wybór i ew. dodawanie
            selectedDate = day
            refresh(currentMonth)
            
            val dialog = new AddEventDialog(day, vsp.model.City(1, "Warszawa", "PL"))
            val result = dialog.showAndWait()

            result match {
              case Some(newEvent: vsp.model.CalendarEvent) =>
                vsp.core.CalendarEventService.addEvent(newEvent) match {
                  case Right(_) => 
                    println("UI: Sukces!")
                    refresh(currentMonth)
                  case Left(error) => 
                    println(s"UI: Błąd walidacji: $error")
                }
              case _ => 
            }

          }
        }

        // Łączymy numer dnia z etykietami wydarzeń
        children = Seq(
          new Label(day.getDayOfMonth.toString) {
            style = if (isSelected) "-fx-padding: 2; -fx-font-weight: bold; -fx-text-fill: white;"
                    else "-fx-padding: 2; -fx-font-weight: bold;"
          }
        ) ++ eventLabels ++ moreLabel
      }
      calendarGrid.add(dayCell, column, row)
    }
  }

  private def determineStyle(isSelected: Boolean, isToday: Boolean, isCurrentMonth: Boolean, isHovered: Boolean): String = {
    val base = "-fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 2;"
    val background = if (isSelected) "-fx-background-color: #3498db;" 
                     else if (isHovered) "-fx-background-color: #ebf5fb;"
                     else if (isToday) "-fx-background-color: #ecf0f1;" 
                     else if (isCurrentMonth) "-fx-background-color: white;"
                     else "-fx-background-color: #f9f9f9;"
    val border = if (isSelected) "-fx-border-color: #2980b9;" 
                 else if (isHovered) "-fx-border-color: #3498db;"
                 else if (isToday) "-fx-border-color: #3498db;" 
                 else "-fx-border-color: #ddd;"
    s"$base $background $border"
  }

  refresh(initialDate)
}