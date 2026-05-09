package vsp.ui.dialogs

import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.layout.GridPane
import scalafx.geometry.Insets
import java.time.{LocalDate, LocalTime}
import java.time.format.DateTimeFormatter
import vsp.model.{CalendarEvent, City}

class AddEventDialog(initialDate: LocalDate, defaultCity: City) extends Dialog[CalendarEvent] {
  
  title = "Add New Event"
  headerText = "Plan your session details"

  val addButtonType = new ButtonType("Add Event", ButtonBar.ButtonData.OKDone)
  dialogPane().buttonTypes = Seq(addButtonType, ButtonType.Cancel)

  // Formatter, żeby godziny zawsze wyglądały tak samo (np. 09:00)
  val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  // --- POLA FORMULARZA ---
  val titleField = new TextField { promptText = "Event Title" }
  val datePicker = new DatePicker { value = initialDate; maxWidth = Double.MaxValue }
  
  // Godzina początkowa
  val startTimeField = new TextField { 
    promptText = "09:00"
    text = "09:00" 
  }
  
  // Godzina końcowa
  val endTimeField = new TextField { 
    promptText = "10:00"
    text = "10:00" 
  }

  // --- AUTOMATYZACJA (+1 godzina) ---
  startTimeField.text.onChange { (_, _, newValue) =>
    try {
      // Próbujemy sparsować to, co wpisał użytkownik
      val start = LocalTime.parse(newValue, timeFormatter)
      // Jeśli się udało, ustawiamy godzinę końcową na +1h
      endTimeField.text = start.plusHours(1).format(timeFormatter)
    } catch {
      case _: Exception => // Ignorujemy błędy podczas pisania (np. gdy pole jest puste)
    }
  }

  val locationField = new TextField { promptText = "Location (optional)" }
  val descArea = new TextArea { promptText = "Description (optional)"; prefRowCount = 3 }

  // --- UKŁAD FORMULARZA ---
  val grid = new GridPane {
    hgap = 10; vgap = 10; padding = Insets(20)

    add(new Label("Title:"), 0, 0)
    add(titleField, 1, 0)
    
    add(new Label("Date:"), 0, 1)
    add(datePicker, 1, 1)
    
    add(new Label("Start Time:"), 0, 2)
    add(startTimeField, 1, 2)
    
    add(new Label("End Time:"), 0, 3)
    add(endTimeField, 1, 3)
    
    add(new Label("Location:"), 0, 4)
    add(locationField, 1, 4)
    
    add(new Label("Description:"), 0, 5)
    add(descArea, 1, 5)
  }

  dialogPane().content = grid

  // --- LOGIKA ZAPISU ---
  resultConverter = (buttonType: ButtonType) => {
    if (buttonType == addButtonType) {
      val finalDate = datePicker.value.value 
      
      CalendarEvent.create(
        title = titleField.text.value,
        city = defaultCity,
        description = descArea.text.value,
        startTime = finalDate.atTime(LocalTime.parse(startTimeField.text.value, timeFormatter)),
        endTime = finalDate.atTime(LocalTime.parse(endTimeField.text.value, timeFormatter))
      )
    } else null
  }
}