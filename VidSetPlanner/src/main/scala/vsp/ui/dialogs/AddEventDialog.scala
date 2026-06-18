package vsp.ui.dialogs

import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import vsp.model.{CalendarEvent, City}
import vsp.core.CityService // <-- NOWY IMPORT!

// ZMIANA 1: Usuwamy domyślne miasto z konstruktora
class AddEventDialog(initialDate: LocalDate) extends Dialog[CalendarEvent] {

  title = "Add New Event"
  headerText = ""
  
  private val labelStyle = "-fx-text-fill: #374151; -fx-font-size: 13px; -fx-font-weight: bold;"
  private val inputBaseStyle = "-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;"
  private val errorInputStyle = "-fx-border-color: #ef4444; -fx-border-width: 1.5;"

  val addButtonType = new ButtonType("Add Event", ButtonBar.ButtonData.OKDone)
  val cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CancelClose)
  dialogPane().buttonTypes = Seq(cancelButtonType, addButtonType)

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

  sH.value.onChange { (_, _, h) => eH.getValueFactory.setValue((h + 1) % 24) }
  sM.value.onChange { (_, _, m) => eM.getValueFactory.setValue(m) }

  // ZMIANA 2: Pole lokalizacji staje się polem wymaganego Miasta
  val cityField = new TextField { 
    promptText = "Enter city (e.g. Warszawa, London)"
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
      new VBox(6, new Label("City") { style = labelStyle }, cityField),
      new VBox(6, new Label("Description") { style = labelStyle }, descArea),
      errorLabel
    )
  }

  dialogPane().content = content

  dialogPane().style = "-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12;"
  
  private val addBtn = dialogPane().lookupButton(addButtonType).asInstanceOf[javafx.scene.control.Button]
  private val cancelBtn = dialogPane().lookupButton(cancelButtonType).asInstanceOf[javafx.scene.control.Button]

  addBtn.style = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;"
  cancelBtn.style = "-fx-background-color: white; -fx-text-fill: #374151; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;"

  // ZMIANA 3: Tymczasowa zmienna na miasto (jeśli geokodowanie się powiedzie)
  private var resolvedCity: Option[City] = None

  addBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    titleField.style = inputBaseStyle
    datePicker.style = inputBaseStyle
    cityField.style = inputBaseStyle // Reset stylu miasta
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

    // Walidacja standardowa
    if (titleField.text.value.trim.isEmpty) {
      isValid = false
      errorMsg = "Title is required"
      titleField.style = inputBaseStyle + errorInputStyle
    } else if (cityField.text.value.trim.isEmpty) {
      isValid = false
      errorMsg = "City is required"
      cityField.style = inputBaseStyle + errorInputStyle
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

    // ZMIANA 4: Jeśli formularz jest ok, łączymy się z API Mikołaja!
    if (isValid) {
      CityService.resolveCity(cityField.text.value.trim) match {
        case Right(city) => 
          resolvedCity = Some(city) // Mamy współrzędne!
        case Left(err) =>
          isValid = false
          // Prawdopodobnie wpisano złe miasto lub brak neta
          errorMsg = "Could not find this city. Please check spelling."
          cityField.style = inputBaseStyle + errorInputStyle
      }
    }

    if (!isValid) {
      errorLabel.text = errorMsg
      errorLabel.visible = true
      errorLabel.managed = true
      e.consume()
    }
  })

  // Potwierdzenie anulowania
  cancelBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    val isFormDirty = titleField.text.value.nonEmpty || descArea.text.value.nonEmpty || cityField.text.value.nonEmpty
    
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
        case _ => e.consume()
      }
    }
  })

  // ZMIANA 5: Przekazujemy prawdzie miasto do bazy
  resultConverter = (bt: ButtonType) => {
    // resultConverter wykona się tylko jeśli addEventFilter nie zrobił e.consume()
    if (bt == addButtonType && resolvedCity.isDefined) {
      val start = datePicker.value.value.atTime(sH.value.value, sM.value.value)
      var end = datePicker.value.value.atTime(eH.value.value, eM.value.value)
      if (end.isBefore(start)) end = end.plusDays(1)

      CalendarEvent.create(
        title = titleField.text.value.trim,
        city = resolvedCity.get, // <--- Tutaj dodajemy miasto znalezione na mapie
        description = descArea.text.value.trim,
        startTime = start,
        endTime = end
      )
    } else null
  }
}