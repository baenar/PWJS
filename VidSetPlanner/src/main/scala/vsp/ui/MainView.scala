package vsp.ui

import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.scene.control.{Button, Label}
import scalafx.geometry.Insets
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import vsp.ui.calendar.MonthGridView

class MainView extends BorderPane {
  private var currentMonth = LocalDate.now()
  private val monthGrid = new MonthGridView(currentMonth)

  // 1. Definiujemy labelkę tutaj, aby była dostępna w całym obiekcie
  private val titleLabel = new Label {
    text = formatTitle(currentMonth)
    style = "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
  }

  private def formatTitle(date: LocalDate): String = {
    val monthName = date.getMonth.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    s"${monthName.capitalize} ${date.getYear}"
  }

  // Pasek górny
  val header = new HBox {
    padding = Insets(20)
    spacing = 20
    alignment = scalafx.geometry.Pos.CenterLeft
    
    val prevBtn = new Button("<") {
      style = "-fx-cursor: hand;"
      onAction = _ => {
        currentMonth = currentMonth.minusMonths(1)
        updateView()
      }
    }
    
    val nextBtn = new Button(">") {
      style = "-fx-cursor: hand;"
      onAction = _ => {
        currentMonth = currentMonth.plusMonths(1)
        updateView()
      }
    }
    
    children = Seq(titleLabel, prevBtn, nextBtn)
  }
  
  def updateView(): Unit = {
    titleLabel.text = formatTitle(currentMonth)
    monthGrid.refresh(currentMonth)
  }

  top = header
  center = monthGrid
  style = "-fx-background-color: #f4f4f4;"
}