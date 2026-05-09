package vsp.ui.calendar

import scalafx.scene.layout.{GridPane, Priority, VBox, ColumnConstraints, RowConstraints}
import scalafx.scene.control.Label
import java.time.LocalDate
import vsp.util.DateUtils
import scalafx.Includes._ // Niezbędne do obsługi zdarzeń onMouseClicked

class MonthGridView(initialDate: LocalDate) extends GridPane {
  // 1. Zmienna przechowująca aktualnie zaznaczoną datę (domyślnie "dzisiaj")
  private var selectedDate: LocalDate = LocalDate.now()

  hgap = 5
  vgap = 5
  hgrow = Priority.Always
  vgrow = Priority.Always

  // Konfiguracja kolumn (14.28% każda)
  columnConstraints = (1 to 7).map(_ => new ColumnConstraints {
    percentWidth = 100.0 / 7
    hgrow = Priority.Always
  })

  def refresh(currentMonth: LocalDate): Unit = {
    children.clear()
    val days = DateUtils.getDaysForMonthGrid(currentMonth)
    val rowCount = Math.ceil(days.length.toDouble / 7).toInt
    
    rowConstraints = (1 to rowCount).map(_ => new RowConstraints {
      percentHeight = 100.0 / rowCount
      vgrow = Priority.Always
    })

    days.zipWithIndex.foreach { case (day, index) =>
      val column = index % 7
      val row = index / 7

      val dayCell = new VBox {
        maxWidth = Double.MaxValue
        maxHeight = Double.MaxValue
        
        // 2. Logika stylizacji (Podświetlenie)
        val isSelected = day == selectedDate
        val isToday = day == LocalDate.now()
        val isCurrentMonth = day.getMonth == currentMonth.getMonth

        style = determineStyle(isSelected, isToday, isCurrentMonth)
        
        // 3. Obsługa kliknięcia
        onMouseClicked = _ => {
          selectedDate = day       // Ustawiamy nową wybraną datę
          refresh(currentMonth)    // Odświeżamy widok, by przerysować ramki
        }

        children = Seq(
          new Label(day.getDayOfMonth.toString) {
            style = if (isSelected) "-fx-padding: 5; -fx-font-weight: bold; -fx-text-fill: white;"
                    else "-fx-padding: 5; -fx-font-weight: bold;"
          }
        )
      }

      add(dayCell, column, row)
    }
  }

  // Funkcja pomocnicza do budowania stylów CSS
  private def determineStyle(isSelected: Boolean, isToday: Boolean, isCurrentMonth: Boolean): String = {
    val base = "-fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 2;"
    
    val background = if (isSelected) "-fx-background-color: #3498db;" // Niebieski dla zaznaczonego
                     else if (isToday) "-fx-background-color: #ecf0f1;" // Jasnoszary dla dzisiejszego
                     else if (isCurrentMonth) "-fx-background-color: white;"
                     else "-fx-background-color: #f9f9f9;"

    val border = if (isSelected) "-fx-border-color: #2980b9;" 
                 else if (isToday) "-fx-border-color: #3498db;" // Niebieska ramka dla dzisiejszego
                 else "-fx-border-color: #ddd;"

    s"$base $background $border"
  }

  refresh(initialDate)
}