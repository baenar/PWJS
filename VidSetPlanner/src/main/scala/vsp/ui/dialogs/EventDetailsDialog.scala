package vsp.ui.dialogs

import scalafx.Includes._
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import vsp.model.CalendarEvent
import java.time.{LocalDateTime, LocalDate}
import java.time.format.DateTimeFormatter
import scalafx.scene.image.{Image, ImageView}

class EventDetailsDialog(initialEvent: CalendarEvent, onUpdate: () => Unit = () => {}) extends Dialog[Unit] { 
  private var currentEvent = initialEvent

  title = "Event Details"
  headerText = "" // Usuwamy domyślny nagłówek dla estetyki

  // --- STYLE WIZUALNE (Jak w AddEventDialog) ---
  private val labelStyle = "-fx-text-fill: #374151; -fx-font-size: 13px; -fx-font-weight: bold;"
  private val inputBaseStyle = "-fx-background-color: white; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8;"
  private val errorInputStyle = "-fx-border-color: #ef4444; -fx-border-width: 1.5;"

  // --- PRZYCISKI ---
  val editButtonType   = new ButtonType("Edit", ButtonBar.ButtonData.Other)
  val saveButtonType   = new ButtonType("Save Changes", ButtonBar.ButtonData.OKDone)
  val cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CancelClose)
  val deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.Left)
  val closeButtonType  = new ButtonType("Close", ButtonBar.ButtonData.CancelClose)
  
  dialogPane().buttonTypes = Seq(deleteButtonType, editButtonType, saveButtonType, cancelButtonType, closeButtonType)

  val editBtn   = dialogPane().lookupButton(editButtonType).asInstanceOf[javafx.scene.control.Button]
  val saveBtn   = dialogPane().lookupButton(saveButtonType).asInstanceOf[javafx.scene.control.Button]
  val cancelBtn = dialogPane().lookupButton(cancelButtonType).asInstanceOf[javafx.scene.control.Button]
  val deleteBtn = dialogPane().lookupButton(deleteButtonType).asInstanceOf[javafx.scene.control.Button]
  val closeBtn  = dialogPane().lookupButton(closeButtonType).asInstanceOf[javafx.scene.control.Button]

  // Aplikowanie stylów do przycisków
  val primaryBtnStyle = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand;"
  val secondaryBtnStyle = "-fx-background-color: white; -fx-text-fill: #374151; -fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;"
  val dangerBtnStyle = "-fx-background-color: white; -fx-text-fill: #ef4444; -fx-border-color: #f87171; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 20; -fx-cursor: hand;"

  editBtn.style = primaryBtnStyle
  saveBtn.style = primaryBtnStyle
  closeBtn.style = secondaryBtnStyle
  cancelBtn.style = secondaryBtnStyle
  deleteBtn.style = dangerBtnStyle

  // Styling okna głównego
  dialogPane().style = "-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12;"

  // --- POLA EDYCJI ---
  val titleInput = new TextField { style = inputBaseStyle }
  val descInput  = new TextArea { prefRowCount = 3; wrapText = true; style = inputBaseStyle + "-fx-padding: 5;" }
  val datePicker = new DatePicker { maxWidth = Double.MaxValue; style = inputBaseStyle }
  
  def createSpinner() = new Spinner[Int](0, 59, 0) { prefWidth = 80; editable = true; style = inputBaseStyle }
  
  val sH = new Spinner[Int](0, 23, 12) { prefWidth = 80; editable = true; style = inputBaseStyle }
  val sM = createSpinner()
  val eH = new Spinner[Int](0, 23, 13) { prefWidth = 80; editable = true; style = inputBaseStyle }
  val eM = createSpinner()

  sH.value.onChange { (_, _, newHour) => eH.getValueFactory.setValue((newHour + 1) % 24) }
  sM.value.onChange { (_, _, newMinute) => eM.getValueFactory.setValue(newMinute) }

  val errorLabel = new Label("") {
    style = "-fx-text-fill: #ef4444; -fx-font-weight: 600; -fx-font-size: 12px;"
    managed = false
    visible = false
  }

  val mainLayout = new VBox(16) { 
    padding = Insets(10, 20, 20, 20)
    prefWidth = 420
    minHeight = 400
  }
  dialogPane().content = mainLayout

  private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

  def showViewMode(): Unit = {
    editBtn.visible = true; editBtn.managed = true
    deleteBtn.visible = true; deleteBtn.managed = true
    closeBtn.visible = true; closeBtn.managed = true
    saveBtn.visible = false; saveBtn.managed = false
    cancelBtn.visible = false; cancelBtn.managed = false

    // 1. Sekcja lewa (Tytuł i Miasto)
    val titleBox = new VBox(4,
      new Label(currentEvent.title) { style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #111827;" },
      new Label(s"📍 ${currentEvent.city.name}") { style = "-fx-font-size: 13px; -fx-text-fill: #6b7280; -fx-font-weight: 500;" }
    )

    // 2. Sekcja prawa (Wielka pogoda + Tooltip)
    val weatherBox = currentEvent.weather match {
      case Some(w) =>
        val iconView = getWeatherIcon(w) // Pobieramy nasz graficzny obrazek .png
        val tempString = currentEvent.temperature.map(t => f"$t%.1f°C").getOrElse("")
        
        val weatherTooltip = new Tooltip {
          text = s"Weather: $w"
          style = "-fx-font-size: 13px; -fx-background-color: rgba(17, 24, 39, 0.9);"
        }

        // Najłatwiejszym sposobem na przypięcie dymku Tooltip i cienia do grafiki
        // jest opakowanie jej w czysty (pusty) Label.
        val iconLabel = new Label {
          graphic = iconView
          tooltip = weatherTooltip
          style = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 2);"
        }

        val tempLabel = new Label(tempString) {
          style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;"
          tooltip = weatherTooltip
        }

        val box = new VBox(2, iconLabel, tempLabel) {
          alignment = Pos.TopCenter
          padding = Insets(0, 0, 0, 20)
        }
        Some(box)

      case None => None
    }

    // 3. Łączymy górę za pomocą "sprężyny" (Region), która rozpycha na boki
    val spacer = new Region { hgrow = Priority.Always }
    
    val topHeader = new HBox {
      alignment = Pos.TopLeft
      children = weatherBox match {
        case Some(wBox) => Seq(titleBox, spacer, wBox) // Jeśli jest pogoda, wrzuć ją na prawo
        case None       => Seq(titleBox)               // Jeśli nie, zostaw sam tytuł
      }
    }

    // 4. Podstawowe wiersze (data, czas)
    val infoRows = Seq(
      new HBox(12,
        new Label("📅") { style = "-fx-font-size: 18px;" },
        new Label(currentEvent.startTime.toLocalDate.format(dateFormatter)) { style = "-fx-font-size: 15px; -fx-text-fill: #374151;" }
      ),
      new HBox(12,
        new Label("🕒") { style = "-fx-font-size: 18px;" },
        new Label(s"${currentEvent.startTime.toLocalTime} — ${currentEvent.endTime.toLocalTime}") { style = "-fx-font-size: 15px; -fx-text-fill: #374151;" }
      )
    )

    // 5. Składamy to w jedną całość
    mainLayout.children.clear()
    mainLayout.children = Seq(
      topHeader, // Nasz nowy, wspaniały pasek z tytułem po lewej i pogodą po prawej!
      new Separator { padding = Insets(5, 0, 5, 0) }, // Subtelna linia oddzielająca
      
      new VBox(10) { children = infoRows },
      
      new Region { prefHeight = 10 },
      new VBox(6, 
        new Label("Description") { style = labelStyle },
        new Label(if(currentEvent.description.isEmpty) "No description provided." else currentEvent.description) { 
          wrapText = true
          style = "-fx-text-fill: #4b5563; -fx-font-size: 14px; -fx-line-spacing: 0.3em;"
        }
      )
    )
  }

// Funkcja mapująca tekst na konkretny plik .png
  private def getWeatherIcon(weather: String): ImageView = {
    val w = weather.toLowerCase

    val iconName = w match {
      // Słońce
      case x if x.contains("clear") || x.contains("sunny") => "sunny.png"
      case x if x.contains("mostly sunny")                 => "mostly_sunny.png"
      
      // Chmury
      case x if x.contains("partly cloudy")                => "partly_cloudy.png"
      case x if x.contains("mostly cloudy")                => "mostly_cloudy_day.png"
      case x if x.contains("overcast") || x.contains("cloud") => "cloudy.png"
      
      // Mgła, dym itp.
      case x if x.contains("mist") || x.contains("fog") || x.contains("haze") || x.contains("smoke") => "haze_fog_dust_smoke.png"
      
      // Deszcz i mżawka
      case x if x.contains("drizzle")                      => "drizzle.png"
      case x if x.contains("scattered shower")             => "scattered_showers_day.png"
      case x if x.contains("heavy rain")                   => "heavy_rain.png"
      case x if x.contains("rain") || x.contains("shower") => "showers_rain.png"
      
      // Burze
      case x if x.contains("isolated") || x.contains("scattered tstorm") => "isolated_scattered_tstorms.png" // Domniemana nazwa z uciętego screena
      case x if x.contains("thunder") || x.contains("storm") => "strong_tstorms.png"
      case x if x.contains("tornado")                      => "tornado.png"
      
      // Śnieg i lód
      case x if x.contains("sleet") || x.contains("hail")  => "sleet_hail.png"
      case x if x.contains("blizzard")                     => "blizzard.png"
      case x if x.contains("blowing snow")                 => "blowing_snow.png"
      case x if x.contains("heavy snow")                   => "heavy_snow.png"
      case x if x.contains("flurries")                     => "flurries.png"
      case x if x.contains("snow shower")                  => "snow_showers_snow.png" // Domniemana nazwa
      case x if x.contains("wintry") || x.contains("mix")  => "wintry_mix_rain_snow.png" // Domniemana nazwa
      case x if x.contains("snow")                         => "snow_showers_snow.png"
      
      // Domyślny obrazek w razie braku dopasowania
      case _                                               => "partly_cloudy.png" 
    }

    // Bezpieczne wczytywanie obrazka
    val stream = getClass.getResourceAsStream(s"/icons/$iconName")
    val imgView = new ImageView()
    
    if (stream != null) {
      imgView.image = new Image(stream)
      imgView.fitWidth = 48
      imgView.fitHeight = 48
      imgView.preserveRatio = true
    } else {
      println(s"[BŁĄD GRAFIKI] Nie znaleziono pliku: /icons/$iconName")
    }
    
    imgView
  }

  // --- TRYB EDYCJI (Formularz) ---
  def showEditMode(): Unit = {
    editBtn.visible = false; editBtn.managed = false
    deleteBtn.visible = false; deleteBtn.managed = false
    closeBtn.visible = false; closeBtn.managed = false
    saveBtn.visible = true; saveBtn.managed = true
    cancelBtn.visible = true; cancelBtn.managed = true

    errorLabel.visible = false
    errorLabel.managed = false
    titleInput.style = inputBaseStyle
    datePicker.style = inputBaseStyle
    val resetTimeStyle = inputBaseStyle + "-fx-pref-width: 80;"
    sH.style = resetTimeStyle; sM.style = resetTimeStyle; eH.style = resetTimeStyle; eM.style = resetTimeStyle

    titleInput.text = currentEvent.title
    descInput.text = currentEvent.description
    datePicker.value = currentEvent.startTime.toLocalDate
    
    sH.getValueFactory.setValue(currentEvent.startTime.getHour)
    sM.getValueFactory.setValue(currentEvent.startTime.getMinute)
    eH.getValueFactory.setValue(currentEvent.endTime.getHour)
    eM.getValueFactory.setValue(currentEvent.endTime.getMinute)

    mainLayout.children.clear()
    mainLayout.children = Seq(
      new VBox(6, new Label("Event Title") { style = labelStyle }, titleInput),
      new VBox(6, new Label("Date") { style = labelStyle }, datePicker),
      new HBox(20,
        new VBox(6, new Label("Start Time") { style = labelStyle }, new HBox(5, sH, new Label(":") { alignment = Pos.Center; style = "-fx-font-weight: bold; -fx-padding: 5 0 0 0;" }, sM)),
        new VBox(6, new Label("End Time") { style = labelStyle }, new HBox(5, eH, new Label(":") { alignment = Pos.Center; style = "-fx-font-weight: bold; -fx-padding: 5 0 0 0;" }, eM))
      ),
      new VBox(6, new Label("Location") { style = labelStyle }, 
        new Label(currentEvent.city.name) { 
          style = inputBaseStyle + "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280;" // Szare pole read-only
          maxWidth = Double.MaxValue
        }
      ),
      new VBox(6, new Label("Description") { style = labelStyle }, descInput),
      errorLabel
    )
  }

  // --- LOGIKA PRZYCISKÓW ---
  editBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    showEditMode()
    e.consume()
  })

cancelBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    // 1. ZAWSZE zatrzymujemy domyślne zamknięcie całego okna EventDetailsDialog
    e.consume()

    // 2. Sprawdzamy, czy użytkownik zmienił jakiekolwiek dane
    val isFormDirty = titleInput.text.value != currentEvent.title || 
                      descInput.text.value != currentEvent.description || 
                      datePicker.value.value != currentEvent.startTime.toLocalDate ||
                      sH.value.value != currentEvent.startTime.getHour || 
                      sM.value.value != currentEvent.startTime.getMinute ||
                      eH.value.value != currentEvent.endTime.getHour || 
                      eM.value.value != currentEvent.endTime.getMinute
    
    if (isFormDirty) {
      val confirmationAlert = new Alert(Alert.AlertType.Confirmation) {
        title = "Discard Changes"
        headerText = ""
        contentText = "Are you sure you want to discard your changes?\nAll unsaved modifications will be lost."
        dialogPane().style = "-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12;"
      }

      val result = confirmationAlert.showAndWait()
      result match {
        case Some(ButtonType.OK) =>
          // Użytkownik godzi się na utratę zmian -> wyłączamy tryb edycji
          showViewMode() 
        case _ =>
          // Użytkownik kliknął Cancel w małym okienku -> NIE robimy showViewMode(), 
          // dzięki czemu zostaje w trybie edycji i może dalej pisać
      }
    } else {
      // Jeśli formularz nie był "brudny" (nic nie zmieniono), od razu wyłączamy tryb edycji bez pytań
      showViewMode()
    }
  })

saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    // 1. ZAWSZE blokujemy domyślne zamykanie okna przez ten przycisk!
    e.consume()

    titleInput.style = inputBaseStyle
    datePicker.style = inputBaseStyle
    val resetTimeStyle = inputBaseStyle + "-fx-pref-width: 80;"
    sH.style = resetTimeStyle; sM.style = resetTimeStyle; eH.style = resetTimeStyle; eM.style = resetTimeStyle
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

    if (titleInput.text.value.trim.isEmpty) {
      isValid = false
      errorMsg = "Title is required"
      titleInput.style = inputBaseStyle + errorInputStyle
    } else if (startDateTime.isBefore(LocalDateTime.now()) && currentEvent.startTime.isAfter(LocalDateTime.now())) {
      isValid = false
      errorMsg = "Cannot move a future event to the past"
      datePicker.style = inputBaseStyle + errorInputStyle
    } else if (startDateTime.isEqual(endDateTime)) {
      isValid = false
      errorMsg = "Event must have a duration"
      sH.style = resetTimeStyle + errorInputStyle
      eH.style = resetTimeStyle + errorInputStyle
    }

    if (!isValid) {
      errorLabel.text = errorMsg
      errorLabel.visible = true
      errorLabel.managed = true
      // Nie musimy już dawać tutaj e.consume(), bo zrobiliśmy to na samej górze
    } else {
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
            showViewMode() // Wracamy do trybu podglądu (okno zostaje otwarte)     
            onUpdate()             
          case Left(err) => 
            errorLabel.text = s"Database Error: $err"
            errorLabel.visible = true
            errorLabel.managed = true
        }
      } catch { 
        case _: Exception => 
          errorLabel.text = "Unexpected error while saving."
          errorLabel.visible = true
          errorLabel.managed = true
      }
    }
  })

  deleteBtn.addEventFilter(javafx.event.ActionEvent.ACTION, (e: javafx.event.ActionEvent) => {
    val confirmationAlert = new Alert(Alert.AlertType.Confirmation) {
      title = "Confirm Deletion"
      headerText = ""
      contentText = s"Do you really want to delete '${currentEvent.title}'?\nThis action cannot be undone."
      
      // Styling okna dialogowego alertu, żeby też pasował
      dialogPane().style = "-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12;"
    }

    val result = confirmationAlert.showAndWait()

    result match {
      case Some(ButtonType.OK) =>
        vsp.core.CalendarEventService.removeEvent(currentEvent.id)
        onUpdate()
      case _ =>
        e.consume() 
    }
  })

  showViewMode()
}