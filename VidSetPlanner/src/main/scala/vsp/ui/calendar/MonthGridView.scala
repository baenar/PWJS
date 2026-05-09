package vsp.ui.calendar

import scalafx.scene.layout.{GridPane, Priority, VBox, HBox, ColumnConstraints, RowConstraints}
import scalafx.scene.control.{Button, Label}
import scalafx.geometry.{Insets, Pos}
import java.time.LocalDate
import vsp.util.DateUtils
import scalafx.Includes._
import vsp.ui.dialogs.AddEventDialog

class MonthGridView(initialDate: LocalDate) extends VBox {
  
  spacing = 5 // Nieco mniejszy odstęp między elementami
  padding = Insets(10)
  vgrow = Priority.Always

  private var selectedDate: LocalDate = LocalDate.now()

  // 1. Pasek z przyciskiem PLUS (Header)
  private val toolbar = new HBox {
    alignment = Pos.CenterRight
    padding = Insets(0, 0, 10, 0)
    val plusBtn = new Button("+") {
      style = """
        -fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 18px; 
        -fx-font-weight: bold; -fx-background-radius: 50; -fx-min-width: 40px; 
        -fx-min-height: 40px; -fx-cursor: hand;
      """
      onAction = _ => {
        val dialog = new AddEventDialog(LocalDate.now(), vsp.model.City(1, "Warszawa", "PL"))
        dialog.showAndWait()
      }
    }
    children = Seq(plusBtn)
  }

  // 2. NOWOŚĆ: Nagłówki dni tygodnia
  private val weekdayHeader = new GridPane {
    hgap = 5
    // Te same ColumnConstraints co w głównej siatce, by kolumny się pokrywały
    columnConstraints = (1 to 7).map(_ => new ColumnConstraints {
      percentWidth = 100.0 / 7
      hgrow = Priority.Always
    })

    val dayNames = Seq("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    dayNames.zipWithIndex.foreach { case (name, i) =>
      val label = new Label(name) {
        maxWidth = Double.MaxValue
        alignment = Pos.Center // Centrowanie tekstu
        style = "-fx-font-weight: bold; -fx-text-fill: #7f8c8d; -fx-padding: 5;"
      }
      add(label, i, 0)
    }
  }

  // 3. Główna siatka kalendarza
  private val calendarGrid = new GridPane {
    hgap = 5
    vgap = 5
    hgrow = Priority.Always
    vgrow = Priority.Always
    columnConstraints = (1 to 7).map(_ => new ColumnConstraints {
      percentWidth = 100.0 / 7
      hgrow = Priority.Always
    })
  }

  // Układamy wszystko pionowo: Toolbar -> Dni Tygodnia -> Siatka Dni
  children = Seq(toolbar, weekdayHeader, calendarGrid)

  def refresh(currentMonth: LocalDate): Unit = {
    calendarGrid.children.clear()
    val days = DateUtils.getDaysForMonthGrid(currentMonth)
    val rowCount = Math.ceil(days.length.toDouble / 7).toInt
    
    calendarGrid.rowConstraints = (1 to rowCount).map(_ => new RowConstraints {
      percentHeight = 100.0 / rowCount
      vgrow = Priority.Always
    })

    days.zipWithIndex.foreach { case (day, index) =>
      val column = index % 7
      val row = index / 7

      val dayCell = new VBox {
        maxWidth = Double.MaxValue
        maxHeight = Double.MaxValue
        val isSelected = day == selectedDate
        val isToday = day == LocalDate.now()
        val isCurrentMonth = day.getMonth == currentMonth.getMonth

        style = determineStyle(isSelected, isToday, isCurrentMonth)
        
        onMouseClicked = _ => {
          selectedDate = day
          refresh(currentMonth)
          val dialog = new AddEventDialog(day, vsp.model.City(1, "Warszawa", "PL"))
          dialog.showAndWait() 
        }

        children = Seq(
          new Label(day.getDayOfMonth.toString) {
            style = if (isSelected) "-fx-padding: 5; -fx-font-weight: bold; -fx-text-fill: white;"
                    else "-fx-padding: 5; -fx-font-weight: bold;"
          }
        )
      }
      calendarGrid.add(dayCell, column, row)
    }
  }

  private def determineStyle(isSelected: Boolean, isToday: Boolean, isCurrentMonth: Boolean): String = {
    val base = "-fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 2;"
    val background = if (isSelected) "-fx-background-color: #3498db;" 
                     else if (isToday) "-fx-background-color: #ecf0f1;" 
                     else if (isCurrentMonth) "-fx-background-color: white;"
                     else "-fx-background-color: #f9f9f9;"
    val border = if (isSelected) "-fx-border-color: #2980b9;" 
                 else if (isToday) "-fx-border-color: #3498db;" 
                 else "-fx-border-color: #ddd;"
    s"$base $background $border"
  }

  refresh(initialDate)
}