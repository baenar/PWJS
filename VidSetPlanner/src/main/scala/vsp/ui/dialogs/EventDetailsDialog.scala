package vsp.ui.dialogs

import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.layout.VBox
import scalafx.geometry.Insets
import vsp.model.CalendarEvent
import vsp.core.CalendarEventService

class EventDetailsDialog(event: CalendarEvent) extends Dialog[String] {
  title = "Event Details"
  headerText = s"Details of: ${event.title}"

  val editButtonType = new ButtonType("Edit", ButtonBar.ButtonData.Other)
  val deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.Other)
  dialogPane().buttonTypes = Seq(editButtonType, deleteButtonType, ButtonType.Close)

  val content = new VBox {
    spacing = 10
    padding = Insets(20)
    children = Seq(
      new Label(s"Title: ${event.title}") { style = "-fx-font-weight: bold; -fx-font-size: 16px;" },
      new Label(s"Time: ${event.startTime.toLocalTime} - ${event.endTime.toLocalTime}"),
      new Label(s"Location: ${event.city.name}"),
      new Separator(),
      new Label("Description:"),
      new Label(if (event.description.isEmpty) "No description" else event.description) {
        wrapText = true
        maxWidth = 300
      }
    )
  }

  dialogPane().content = content

  resultConverter = (buttonType: ButtonType) => {
    if (buttonType == editButtonType) "EDIT"
    else if (buttonType == deleteButtonType) "DELETE"
    else null
  }
}