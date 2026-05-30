package vsp.ui.dialogs

import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.layout.{VBox, HBox, Priority, Region}
import scalafx.geometry.{Insets, Pos}
import vsp.model.CalendarEvent
import java.time.{LocalDateTime, LocalDate}
import java.time.format.DateTimeFormatter

class EventDetailsDialog(initialEvent: CalendarEvent, onUpdate: () => Unit = () => {}) extends Dialog[Unit] { 
  private var currentEvent = initialEvent

  title = "Event Management"
  
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
  val datePicker = new DatePicker { maxWidth = Double.MaxValue }
  
  def createHourSpinner() = new Spinner[Int](0, 23, 12) { prefWidth = 60; editable = true }
  def createMinuteSpinner() = new Spinner[Int](0, 59, 0) { prefWidth = 60; editable = true }
  
  val sH = createHourSpinner(); val sM = createMinuteSpinner()
  val eH = createHourSpinner(); val eM = createMinuteSpinner()

  // Automatyzacja z AddEventDialog
  sH.value.onChange { (_, _, newHour) => eH.getValueFactory.setValue((newHour + 1) % 24) }
  sM.value.onChange { (_, _, newMinute) => eM.getValueFactory.setValue(newMinute) }

  // Kontenery dla czasu wyciągnięte wyżej, żebyśmy mogli podświetlać je na czerwono
  val startBox = new HBox(5, sH, new Label(":"), sM) { alignment = Pos.CenterLeft }
  val endBox = new HBox(5, eH, new Label(":"), eM) { alignment = Pos.CenterLeft }
  val timeRow = new HBox(15, new Label("From:"), startBox, new Label("To:"), endBox) { alignment = Pos.CenterLeft }

  // Etykieta błędu
  val errorLabel = new Label("") {
    style = "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 12px;"
    managed = false
    visible = false
  }

  val mainLayout = new VBox(10) { 
    padding = Insets(25); prefWidth = 400; prefHeight = 460
  }
  dialogPane().content = mainLayout

  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def showViewMode(): Unit = {
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
      new Label(s"📅 ${currentEvent.startTime.toLocalDate.format(dateFormatter)}") { style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;" },
      new Label(s"🕒 ${currentEvent.startTime.toLocalTime} - ${currentEvent.endTime.toLocalTime}") { style = "-fx-font-size: 14px;" },
      new Label(s"📍 ${currentEvent.city.name}") { style = "-fx-font-size: 14px; -fx-text-fill: #2c3e50;" }, 
      new Separator(),
      new Label("DESCRIPTION") { style = "-fx-font-size: 10px; -fx-text-fill: #bdc3c7; -fx-font-weight: bold;" },
      new Label(if(currentEvent.description.isEmpty) "No description" else currentEvent.description) { wrapText = true }
    )
  }

  def showEditMode(): Unit = {
    editBtn.visible = false; editBtn.managed = false
    deleteBtn.visible = false; deleteBtn.managed = false
    saveBtn.visible = true; saveBtn.managed = true
    cancelBtn.visible = true; cancelBtn.managed = true

    // Wypełniamy pola aktualnymi danymi i czyścimy błędy, gdyby jakieś zostały z wcześniej
    errorLabel.visible = false
    errorLabel.managed = false
    titleInput.style = "-fx-font-size: 14px;"
    datePicker.style = ""
    startBox.style = ""
    endBox.style = ""

    titleInput.text = currentEvent.title
    descInput.text = currentEvent.description
    datePicker.value = currentEvent.startTime.toLocalDate
    
    sH.getValueFactory.setValue(currentEvent.startTime.getHour)
    sM.getValueFactory.setValue(currentEvent.startTime.getMinute)
    eH.getValueFactory.setValue(currentEvent.endTime.getHour)
    eM.getValueFactory.setValue(currentEvent.endTime.getMinute)

    mainLayout.children.clear()
    mainLayout.children = Seq(
      new Label("EDIT TITLE") { style = "-fx-font-size: 10px; -fx-text-fill: #3498db; -fx-font-weight: bold;" },
      titleInput,
      new Label("EDIT DATE") { style = "-fx-font-size: 10px; -fx-text-fill: #3498db; -fx-font-weight: bold;" },
      datePicker,
      new Label("EDIT TIME") { style = "-fx-font-size: 10px; -fx-text-fill: #3498db; -fx-font-weight: bold;" },
      timeRow, // Wklejamy przygotowany wcześniej wyżej cały kontener
      new Label(s"Location: ${currentEvent.city.name}") { 
        style = "-fx-font-size: 11px; -fx-text-fill: #95a5a6; -fx-padding: 2 0 2 0;" 
      },
      new Separator(),
      new Label("EDIT DESCRIPTION") { style = "-fx-font-size: 10px; -fx-text-fill: #3498db; -fx-font-weight: bold;" },
      descInput,
      errorLabel // Etykieta błędu ląduje na samym dole
    )
  }

  editBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    showEditMode()
    e.consume()
  })

  cancelBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    showViewMode()
    e.consume()
  })

  // --- WALIDACJA W LOCIE DLA TRYBU EDYCJI ---
  saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    
    // 1. Reset stylów
    titleInput.style = "-fx-font-size: 14px;"
    datePicker.style = ""
    startBox.style = ""
    endBox.style = ""
    errorLabel.visible = false
    errorLabel.managed = false

    var isValid = true
    var errorMsg = ""

    val chosenDate: LocalDate = datePicker.value.value
    val startDateTime = chosenDate.atTime(sH.value.value, sM.value.value)
    var endDateTime = chosenDate.atTime(eH.value.value, eM.value.value)

    if (endDateTime.isBefore(startDateTime)) {
      endDateTime = endDateTime.plusDays(1)
    }

    // 2. Reguła: Pusty tytuł
    if (titleInput.text.value.trim.isEmpty) {
      isValid = false
      errorMsg = "Title cannot be empty!"
      titleInput.style = "-fx-font-size: 14px; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 3;"
    } 
    // 3. Reguła SMART: Przeniesienie wydarzenia w przeszłość (dozwolone, jeśli event JUŻ był w przeszłości)
    else if (startDateTime.isBefore(LocalDateTime.now()) && currentEvent.startTime.isAfter(LocalDateTime.now())) {
      isValid = false
      errorMsg = "Cannot move a future event to the past!"
      datePicker.style = "-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 3;"
      startBox.style = "-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 3; -fx-padding: 2;"
    }
    // 4. Reguła: Ten sam czas startu i zakończenia
    else if (startDateTime.isEqual(endDateTime)) {
      isValid = false
      errorMsg = "Event start and end time cannot be exactly the same!"
      startBox.style = "-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 3; -fx-padding: 2;"
      endBox.style = "-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 3; -fx-padding: 2;"
    }

    // 5. Decyzja
    if (!isValid) {
      errorLabel.text = errorMsg
      errorLabel.visible = true
      errorLabel.managed = true
      e.consume() // Błąd: Zatrzymujemy okno
    } else {
      // Sukces: Zapisujemy do bazy
      try {
        val updated = currentEvent.copy(
          title = titleInput.text.value.trim,
          description = descInput.text.value.trim,
          startTime = startDateTime,
          endTime = endDateTime
        )

        vsp.core.CalendarEventService.updateEvent(updated) match {
          case Right(_) =>
            currentEvent = updated 
            showViewMode()        
            onUpdate()             
          case Left(err) => 
            errorLabel.text = s"Database Error: $err"
            errorLabel.visible = true
            errorLabel.managed = true
            e.consume() // W razie błędu bazy również zostawiamy okno otwarte
        }
      } catch { 
        case _: Exception => 
          errorLabel.text = "Unexpected error while saving."
          errorLabel.visible = true
          errorLabel.managed = true
          e.consume()
      }
    }
    
    // Jeśli isValid == true i brak błędów z bazy, okno naturalnie przejdzie do trybu widoku
    if (!isValid) e.consume() 
  })

deleteBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    // 1. Tworzymy okno z potwierdzeniem
    val confirmationAlert = new Alert(Alert.AlertType.Confirmation) {
      title = "Confirm Deletion"
      headerText = "Delete Event"
      contentText = s"Do you really want to delete '${currentEvent.title}'?\nThis action cannot be undone."
    }

    // 2. Wyświetlamy okno i czekamy na reakcję
    val result = confirmationAlert.showAndWait()

    result match {
      case Some(ButtonType.OK) =>
        // 3. Użytkownik potwierdził -> Usuwamy z bazy i odświeżamy widoki
        vsp.core.CalendarEventService.removeEvent(currentEvent.id)
        onUpdate()
        // NIE używamy e.consume() -> pozwalamy głównemu oknu szczegółów się zamknąć

      case _ =>
        // 4. Użytkownik kliknął Cancel lub zamknął okienko X -> Zatrzymujemy akcję!
        e.consume() 
    }
  })

  showViewMode()
}