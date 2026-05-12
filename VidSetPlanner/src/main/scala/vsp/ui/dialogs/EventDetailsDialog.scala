package vsp.ui.dialogs

import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.layout.{VBox, HBox, Priority, Region}
import scalafx.geometry.{Insets, Pos}
import vsp.model.CalendarEvent
import java.time.LocalDateTime

class EventDetailsDialog(initialEvent: CalendarEvent) extends Dialog[Unit] {
  // Przechowujemy aktualny stan wydarzenia w zmiennej
  private var currentEvent = initialEvent

  title = "Event Management"
  
  // 1. Definicja przycisków w dolnym pasku
  val editButtonType   = new ButtonType("Edit", ButtonBar.ButtonData.Other)
  val saveButtonType   = new ButtonType("Save", ButtonBar.ButtonData.OKDone)
  val cancelButtonType = new ButtonType("Cancel Edit", ButtonBar.ButtonData.CancelClose)
  val deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.Other)
  val closeButtonType  = new ButtonType("Close Window", ButtonBar.ButtonData.Finish)
  
  dialogPane().buttonTypes = Seq(editButtonType, saveButtonType, cancelButtonType, deleteButtonType, closeButtonType)

  val editBtn   = dialogPane().lookupButton(editButtonType)
  val saveBtn   = dialogPane().lookupButton(saveButtonType)
  val cancelBtn = dialogPane().lookupButton(cancelButtonType)
  val deleteBtn = dialogPane().lookupButton(deleteButtonType)

  val titleInput = new TextField { style = "-fx-font-size: 14px;" }
  val descInput  = new TextArea { prefHeight = 70; wrapText = true }
  
  def createTimeField() = new TextField { prefWidth = 45; alignment = Pos.Center }
  val sH = createTimeField(); val sM = createTimeField()
  val eH = createTimeField(); val eM = createTimeField()

  val mainLayout = new VBox(12) {
    padding = Insets(25); prefWidth = 400; prefHeight = 450
  }
  dialogPane().content = mainLayout

  // --- TRYB WIDOKU ---
  def showViewMode(): Unit = {
    // Widoczność przycisków
    editBtn.visible = true; editBtn.managed = true
    deleteBtn.visible = true; deleteBtn.managed = true
    saveBtn.visible = false; saveBtn.managed = false
    cancelBtn.visible = false; cancelBtn.managed = false

    mainLayout.children.clear()
    mainLayout.children = Seq(
      new Label("EVENT TITLE") { style = "-fx-font-size: 10px; -fx-text-fill: #bdc3c7; -fx-font-weight: bold;" },
      new Label(currentEvent.title) { style = "-fx-font-size: 18px; -fx-font-weight: bold;" },
      new Separator(),
      new Label("TIME & PLACE") { style = "-fx-font-size: 10px; -fx-text-fill: #bdc3c7; -fx-font-weight: bold;" },
      new Label(s"🕒 ${currentEvent.startTime.toLocalTime} - ${currentEvent.endTime.toLocalTime}"),
      new Label(s"📍 ${currentEvent.city.name}") { style = "-fx-font-size: 14px; -fx-text-fill: #2c3e50;" }, 
      new Separator(),
      new Label("DESCRIPTION") { style = "-fx-font-size: 10px; -fx-text-fill: #bdc3c7; -fx-font-weight: bold;" },
      new Label(if(currentEvent.description.isEmpty) "No description" else currentEvent.description) { wrapText = true }
    )
  }

  // --- TRYB EDYCJI ---
  def showEditMode(): Unit = {
    // Widoczność przycisków
    editBtn.visible = false; editBtn.managed = false
    deleteBtn.visible = false; deleteBtn.managed = false
    saveBtn.visible = true; saveBtn.managed = true
    cancelBtn.visible = true; cancelBtn.managed = true

    titleInput.text = currentEvent.title
    descInput.text = currentEvent.description
    sH.text = f"${currentEvent.startTime.getHour}%02d"
    sM.text = f"${currentEvent.startTime.getMinute}%02d"
    eH.text = f"${currentEvent.endTime.getHour}%02d"
    eM.text = f"${currentEvent.endTime.getMinute}%02d"

    mainLayout.children.clear()
    mainLayout.children = Seq(
      new Label("EDIT TITLE") { style = "-fx-font-size: 10px; -fx-text-fill: #3498db; -fx-font-weight: bold;" },
      titleInput,
      new Label("EDIT TIME") { style = "-fx-font-size: 10px; -fx-text-fill: #3498db; -fx-font-weight: bold;" },
      new HBox(5, new Label("From:"), sH, new Label(":"), sM, new Region { prefWidth = 10 }, new Label("To:"), eH, new Label(":"), eM),
      new Label(s"Location: ${currentEvent.city.name}") { 
        style = "-fx-font-size: 11px; -fx-text-fill: #95a5a6; -fx-padding: 5 0 0 0;" 
      },
      new Label("EDIT DESCRIPTION") { style = "-fx-font-size: 10px; -fx-text-fill: #3498db; -fx-font-weight: bold;" },
      descInput
    )
  }

  editBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    showEditMode()
    e.consume() // Zostajemy w oknie
  })

  cancelBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    showViewMode()
    e.consume() // Zostajemy w oknie
  })

  saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    try {
      val updated = currentEvent.copy(
        title = titleInput.text.value,
        description = descInput.text.value,
        startTime = currentEvent.startTime.withHour(sH.text.value.toInt).withMinute(sM.text.value.toInt),
        endTime = currentEvent.endTime.withHour(eH.text.value.toInt).withMinute(eM.text.value.toInt)
      )

      vsp.core.CalendarEventService.updateEvent(updated) match {
        case Right(_) =>
          currentEvent = updated // Aktualizujemy lokalny stan okna!
          showViewMode()        // Wracamy do podglądu
        case Left(err) => println(s"Błąd: $err")
      }
    } catch { case _: Exception => println("Błąd formatu czasu") }
    
    e.consume() // Zostajemy w oknie
  })

  // 4. DELETE (usuwa i zamyka okno, bo nie ma już czego wyświetlać)
  deleteBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    vsp.core.CalendarEventService.removeEvent(currentEvent.id)
    // Nie robimy consume(), więc okno się zamknie
  })

  showViewMode()
}