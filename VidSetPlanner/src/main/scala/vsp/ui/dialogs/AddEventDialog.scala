package vsp.ui.dialogs

import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import vsp.model.{CalendarEvent, City}

class AddEventDialog(initialDate: LocalDate, defaultCity: City) extends Dialog[CalendarEvent] {

  title = "Add New Event"
  
  // Usuwamy standardowy header, by zrobić własny, bardziej estetyczny
  headerText = ""
  
  // Style CSS dla komponentów (naśladujące Tailwind/React)
  private val labelStyle = "-fx-text-fill: #374151; -fx-font-size: 13px; -fx-font-weight: bold;"
  private val inputBaseStyle = "-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;"
  private val errorInputStyle = "-fx-border-color: #ef4444; -fx-border-width: 1.5;"

  // Przyciski
  val addButtonType = new ButtonType("Add Event", ButtonBar.ButtonData.OKDone)
  val cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CancelClose)
  dialogPane().buttonTypes = Seq(cancelButtonType, addButtonType)

  // --- POLA FORMULARZA ---
  val titleField = new TextField { 
    promptText = "Enter event title"
    style = inputBaseStyle
  }

  val dateDisplay = new Label(initialDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))) {
    style = "-fx-padding: 10 12; -fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-text-fill: #374151; -fx-font-weight: 500;"
    maxWidth = Double.MaxValue
  }

  val datePicker = new DatePicker { 
    value = initialDate
    maxWidth = Double.MaxValue
    style = inputBaseStyle
  }

  def createSpinner(min: Int, max: Int, default: Int) = new Spinner[Int](min, max, default) {
    prefWidth = 80
    editable = true
    style = inputBaseStyle
  }

  val sH = createSpinner(0, 23, 9)
  val sM = createSpinner(0, 59, 0)
  val eH = createSpinner(0, 23, 10)
  val eM = createSpinner(0, 59, 0)

  // Automatyzacja czasu
  sH.value.onChange { (_, _, h) => eH.getValueFactory.setValue((h + 1) % 24) }
  sM.value.onChange { (_, _, m) => eM.getValueFactory.setValue(m) }

  val locationField = new TextField { 
    promptText = "Add location (optional)"
    style = inputBaseStyle
  }

  val descArea = new TextArea { 
    promptText = "Add description (optional)"
    prefRowCount = 3
    style = inputBaseStyle + "-fx-padding: 5;"
    wrapText = true
  }

  val errorLabel = new Label("") {
    style = "-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: 600;"
    managed = false
    visible = false
  }

  // --- UKŁAD FORMULARZA ---
  val content = new VBox(16) {
    padding = Insets(20)
    prefWidth = 400
    
    children = Seq(
      new VBox(6, new Label("Event Title") { style = labelStyle }, titleField),
      new VBox(6, new Label("Date") { style = labelStyle }, datePicker),
      new HBox(20,
        new VBox(6, new Label("Start Time") { style = labelStyle }, new HBox(5, sH, new Label(":") { alignment = Pos.Center; style = "-fx-font-weight: bold; -fx-padding: 5 0 0 0;" }, sM)),
        new VBox(6, new Label("End Time") { style = labelStyle }, new HBox(5, eH, new Label(":") { alignment = Pos.Center; style = "-fx-font-weight: bold; -fx-padding: 5 0 0 0;" }, eM))
      ),
      new VBox(6, new Label("Location") { style = labelStyle }, locationField),
      new VBox(6, new Label("Description") { style = labelStyle }, descArea),
      errorLabel
    )
  }

  dialogPane().content = content

  // Styling DialogPane i Buttonów
  dialogPane().style = "-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12;"
  
  // Stylowanie przycisków po ich renderowaniu
  private val addBtn = dialogPane().lookupButton(addButtonType).asInstanceOf[javafx.scene.control.Button]
  private val cancelBtn = dialogPane().lookupButton(cancelButtonType).asInstanceOf[javafx.scene.control.Button]

  addBtn.style = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;"
  cancelBtn.style = "-fx-background-color: white; -fx-text-fill: #374151; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;"

  // --- WALIDACJA ---
  addBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    // Reset stylów
    titleField.style = inputBaseStyle
    datePicker.style = inputBaseStyle
    val resetTimeStyle = inputBaseStyle + "-fx-pref-width: 80;"
    sH.style = resetTimeStyle; sM.style = resetTimeStyle
    eH.style = resetTimeStyle; eM.style = resetTimeStyle
    errorLabel.visible = false
    errorLabel.managed = false

    var isValid = true
    var errorMsg = ""

    val startDT = datePicker.value.value.atTime(sH.value.value, sM.value.value)
    var endDT = datePicker.value.value.atTime(eH.value.value, eM.value.value)
    if (endDT.isBefore(startDT)) endDT = endDT.plusDays(1)

    if (titleField.text.value.trim.isEmpty) {
      isValid = false
      errorMsg = "Title is required"
      titleField.style = inputBaseStyle + errorInputStyle
    } else if (startDT.isBefore(LocalDateTime.now())) {
      isValid = false
      errorMsg = "Date/time cannot be in the past"
      datePicker.style = inputBaseStyle + errorInputStyle
    } else if (startDT.isEqual(endDT)) {
      isValid = false
      errorMsg = "Event must have a duration"
      sH.style = resetTimeStyle + errorInputStyle
      eH.style = resetTimeStyle + errorInputStyle
    }

    if (!isValid) {
      errorLabel.text = errorMsg
      errorLabel.visible = true
      errorLabel.managed = true
      e.consume()
    }
  })

  cancelBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    // Sprawdzamy, czy użytkownik w ogóle coś wpisał (żeby nie irytować go pop-upem, jeśli okno jest całkiem puste)
    val isFormDirty = titleField.text.value.nonEmpty || descArea.text.value.nonEmpty || locationField.text.value.nonEmpty
    
    if (isFormDirty) {
      val confirmationAlert = new Alert(Alert.AlertType.Confirmation) {
        title = "Confirm Cancellation"
        headerText = ""
        contentText = "Are you sure you want to cancel adding this event?\nAll unsaved data will be lost."
        dialogPane().style = "-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12;"
      }

      val result = confirmationAlert.showAndWait()
      result match {
        case Some(ButtonType.OK) =>
          // Użytkownik chce wyjść -> NIE robimy consume(), pozwalamy oknu naturalnie się zamknąć
        case _ =>
          // Użytkownik rozmyślił się i chce dalej pisać -> Zatrzymujemy zamykanie
          e.consume()
      }
    }
  })

  // Logika zapisu
  resultConverter = (bt: ButtonType) => {
    if (bt == addButtonType) {
      val start = datePicker.value.value.atTime(sH.value.value, sM.value.value)
      var end = datePicker.value.value.atTime(eH.value.value, eM.value.value)
      if (end.isBefore(start)) end = end.plusDays(1)

      CalendarEvent.create(
        title = titleField.text.value.trim,
        city = defaultCity,
        description = descArea.text.value.trim,
        startTime = start,
        endTime = end
      )
    } else null
  }
}