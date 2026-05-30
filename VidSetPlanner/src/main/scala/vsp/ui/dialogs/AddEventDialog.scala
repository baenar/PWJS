package vsp.ui.dialogs

import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, HBox}
import scalafx.geometry.{Insets, Pos}
import java.time.{LocalDate, LocalDateTime}
import vsp.model.{CalendarEvent, City}

class AddEventDialog(initialDate: LocalDate, defaultCity: City) extends Dialog[CalendarEvent] {
  
  title = "Add New Event"
  headerText = "Plan your session details"

  val addButtonType = new ButtonType("Add Event", ButtonBar.ButtonData.OKDone)
  dialogPane().buttonTypes = Seq(addButtonType, ButtonType.Cancel)

  // --- POLA FORMULARZA ---
  val titleField = new TextField { promptText = "Event Title" }
  val datePicker = new DatePicker { value = initialDate; maxWidth = Double.MaxValue }
  
  def createHourSpinner(default: Int) = new Spinner[Int](0, 23, default) { prefWidth = 65; editable = true }
  def createMinuteSpinner(default: Int) = new Spinner[Int](0, 59, default) { prefWidth = 65; editable = true }

  val sH = createHourSpinner(9)
  val sM = createMinuteSpinner(0)
  val eH = createHourSpinner(10)
  val eM = createMinuteSpinner(0)

  sH.value.onChange { (_, _, newHour) => eH.getValueFactory.setValue((newHour + 1) % 24) }
  sM.value.onChange { (_, _, newMinute) => eM.getValueFactory.setValue(newMinute) }

  val locationField = new TextField { promptText = "Location (optional)" }
  val descArea = new TextArea { promptText = "Description (optional)"; prefRowCount = 3 }

  val startBox = new HBox(5, sH, new Label(":"), sM) { alignment = Pos.CenterLeft }
  val endBox = new HBox(5, eH, new Label(":"), eM) { alignment = Pos.CenterLeft }

  // Etykieta błędu (domyślnie ukryta)
  val errorLabel = new Label("") {
    style = "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 12px;"
    managed = false
    visible = false
  }

  // --- UKŁAD FORMULARZA ---
  val grid = new GridPane {
    hgap = 10; vgap = 10; padding = Insets(20)

    add(new Label("Title:"), 0, 0)
    add(titleField, 1, 0)
    
    add(new Label("Date:"), 0, 1)
    add(datePicker, 1, 1)
    
    add(new Label("Start Time:"), 0, 2)
    add(startBox, 1, 2)
    
    add(new Label("End Time:"), 0, 3)
    add(endBox, 1, 3)
    
    add(new Label("Location:"), 0, 4)
    add(locationField, 1, 4)
    
    add(new Label("Description:"), 0, 5)
    add(descArea, 1, 5)

    add(errorLabel, 1, 6) 
  }

  dialogPane().content = grid

  // --- WALIDACJA W LOCIE (Event Filter) ---
  val addBtnNode = dialogPane().lookupButton(addButtonType)
  addBtnNode.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    // 1. Resetujemy style do domyślnych
    titleField.style = ""
    datePicker.style = ""
    startBox.style = ""
    endBox.style = ""
    errorLabel.visible = false
    errorLabel.managed = false

    var isValid = true
    var errorMsg = ""

    val chosenDate = datePicker.value.value
    val startDateTime = chosenDate.atTime(sH.value.value, sM.value.value)
    var endDateTime = chosenDate.atTime(eH.value.value, eM.value.value)

    // Symulujemy zachowanie logiki zapisu (żeby sprawdzić poprawne daty nocne)
    if (endDateTime.isBefore(startDateTime)) {
      endDateTime = endDateTime.plusDays(1)
    }

    // 2. Reguła: Pusty tytuł
    if (titleField.text.value.trim.isEmpty) {
      isValid = false
      errorMsg = "Title cannot be empty!"
      titleField.style = "-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 3;"
    } 
    // 3. Reguła: Data z przeszłości
    else if (startDateTime.isBefore(LocalDateTime.now())) {
      isValid = false
      errorMsg = "Cannot schedule an event in the past!"
      datePicker.style = "-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 3;"
      startBox.style = "-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 3; -fx-padding: 2;"
    }
    // 4. Reguła: Czas startu i zakończenia jest taki sam
    else if (startDateTime.isEqual(endDateTime)) {
      isValid = false
      errorMsg = "Event start and end time cannot be exactly the same!"
      startBox.style = "-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 3; -fx-padding: 2;"
      endBox.style = "-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 3; -fx-padding: 2;"
    }

    // 5. Pokazanie komunikatu
    if (!isValid) {
      errorLabel.text = errorMsg
      errorLabel.visible = true
      errorLabel.managed = true
      e.consume() // Zatrzymujemy okno
    }
  })

  // --- LOGIKA ZAPISU ---
  resultConverter = (buttonType: ButtonType) => {
    if (buttonType == addButtonType) {
      val finalDate = datePicker.value.value 
      val start = finalDate.atTime(sH.value.value, sM.value.value)
      var end = finalDate.atTime(eH.value.value, eM.value.value)

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