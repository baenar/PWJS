package vsp.ui.calendar

import scalafx.scene.layout.{GridPane, Priority, VBox, HBox, ColumnConstraints, RowConstraints}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.input.MouseEvent
import scalafx.scene.input.MouseButton
import scalafx.geometry.{Insets, Pos}
import java.time.LocalDate
import vsp.util.DateUtils
import scalafx.Includes._
import vsp.ui.dialogs.AddEventDialog
import vsp.persistence.EventRepository
import vsp.model.CalendarEvent
import vsp.ui.dialogs.EventDetailsDialog
//import vsp.ui.dialogs.{SaveAction, DeleteAction}

class MonthGridView(initialDate: LocalDate, onDateSelected: LocalDate => Unit) extends VBox {
  
  spacing = 5
  padding = Insets(10)
  vgrow = Priority.Always

  var selectedDate: LocalDate = LocalDate.now()

  // 2. Nagłówki dni tygodnia
  private val weekdayHeader = new GridPane {
    hgap = 5
    columnConstraints = (1 to 7).map(_ => new ColumnConstraints {
      percentWidth = 100.0 / 7
      hgrow = Priority.Always
    })
    val dayNames = Seq("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    dayNames.zipWithIndex.foreach { case (name, i) =>
      add(new Label(name) {
        maxWidth = Double.MaxValue
        alignment = Pos.Center
        style = "-fx-font-weight: bold; -fx-text-fill: #7f8c8d; -fx-padding: 5;"
      }, i, 0)
    }
  }

  // 3. Główna siatka
  private val calendarGrid = new GridPane {
    hgap = 5; vgap = 5
    hgrow = Priority.Always; vgrow = Priority.Always
    columnConstraints = (1 to 7).map(_ => new ColumnConstraints {
      percentWidth = 100.0 / 7
      hgrow = Priority.Always
    })
  }

  children = Seq(/*toolbar, */weekdayHeader, calendarGrid)

  def refresh(currentMonth: LocalDate): Unit = {
    calendarGrid.children.clear()
    calendarGrid.rowConstraints.clear()

    val allEventsFromDb = EventRepository.findAll()

    val days = DateUtils.getDaysForMonthGrid(currentMonth)
    val rowCount = Math.ceil(days.length.toDouble / 7).toInt
    
    calendarGrid.rowConstraints = (1 to rowCount).map(_ => new RowConstraints {
      percentHeight = 100.0 / rowCount
      vgrow = Priority.Always
      fillHeight = true
    })

    days.zipWithIndex.foreach { case (day, index) =>
      val column = index % 7
      val row = index / 7

      val dayCell = new VBox {
        maxWidth = Double.MaxValue
        maxHeight = Double.MaxValue
        spacing = 2
        padding = Insets(2)

        minHeight = 0
        
        val isSelected = day == selectedDate
        val isToday = day == LocalDate.now()
        val isCurrentMonth = day.getMonth == currentMonth.getMonth

        style = determineStyle(isSelected, isToday, isCurrentMonth, isHovered = false)

        val dayEvents = allEventsFromDb.filter(_.startTime.toLocalDate == day)

        val MAX_VISIBLE_EVENTS = 2
        val visibleEvents = dayEvents.take(MAX_VISIBLE_EVENTS)
        val hiddenCount = dayEvents.size - MAX_VISIBLE_EVENTS

        val eventLabels = dayEvents.take(MAX_VISIBLE_EVENTS).map { event =>
          new Label(event.title) {
            maxWidth = Double.MaxValue
            style = """...Twoje style..."""
            
            onMouseClicked = (e: MouseEvent) => {
              e.consume() // STOP: nie wybieraj dnia pod spodem!
              handleEventInteraction(event, currentMonth)
            }
          }
        }

        val moreLabel = if (hiddenCount > 0) {
          Seq(new Label(s"+$hiddenCount more") {
            maxWidth = Double.MaxValue
            style = s"""
              -fx-text-fill: ${if (isSelected) "white" else "#7f8c8d"};
              -fx-font-size: 9px; 
              -fx-font-weight: bold;
              -fx-padding: 1 4 1 4;
            """
          })
        } else Nil

        onMouseEntered = _ => {
          if (!isSelected) style = determineStyle(isSelected, isToday, isCurrentMonth, isHovered = true)
        }
        onMouseExited = _ => {
          if (!isSelected) style = determineStyle(isSelected, isToday, isCurrentMonth, isHovered = false)
        }

        onMouseClicked = (e: MouseEvent) => {
          e.button match {
            
            case MouseButton.PRIMARY =>
                selectedDate = day
                refresh(currentMonth)
              
            case MouseButton.SECONDARY =>
              val dialog = new AddEventDialog(day, vsp.model.City(1, "Warszawa", "PL"))
              val result = dialog.showAndWait()

              result match {
                case Some(newEvent: vsp.model.CalendarEvent) =>
                  vsp.core.CalendarEventService.addEvent(newEvent) match {
                    case Right(_) => 
                      println("UI: Sukces dodawania prawym przyciskiem!")
                      refresh(currentMonth) // Odśwież, żeby zobaczyć nowy pasek
                    case Left(error) => 
                      println(s"UI: Błąd walidacji: $error")
                  }
                case _ => // Anulowano dialog
              }
            case _ => 
          }
        }
    

        // Łączymy numer dnia z etykietami wydarzeń
        children = Seq(
          new Label(day.getDayOfMonth.toString) {
            style = if (isSelected) "-fx-padding: 2; -fx-font-weight: bold; -fx-text-fill: white;"
                    else "-fx-padding: 2; -fx-font-weight: bold;"
          }
        ) ++ eventLabels ++ moreLabel
      }
      calendarGrid.add(dayCell, column, row)
    }
  }

  private def determineStyle(isSelected: Boolean, isToday: Boolean, isCurrentMonth: Boolean, isHovered: Boolean): String = {
    val base = "-fx-border-radius: 5; -fx-background-radius: 5; -fx-border-width: 2;"
    val background = if (isSelected) "-fx-background-color: #3498db;" 
                     else if (isHovered) "-fx-background-color: #ebf5fb;"
                     else if (isToday) "-fx-background-color: #ecf0f1;" 
                     else if (isCurrentMonth) "-fx-background-color: white;"
                     else "-fx-background-color: #f9f9f9;"
    val border = if (isSelected) "-fx-border-color: #2980b9;" 
                 else if (isHovered) "-fx-border-color: #3498db;"
                 else if (isToday) "-fx-border-color: #3498db;" 
                 else "-fx-border-color: #ddd;"
    s"$base $background $border"
  }

 /* private def handleEventInteraction(event: CalendarEvent, currentMonth: java.time.LocalDate): Unit = {
    val details = new EventDetailsDialog(event)
  
    details.showAndWait() match {
      // 1. Obsługa usuwania (używamy obiektu DeleteAction)
      case Some(DeleteAction) =>
        vsp.core.CalendarEventService.removeEvent(event.id)
        refresh(currentMonth)

      // 2. Obsługa zapisu (używamy SaveAction i nowej metody updateEvent)
      case Some(SaveAction(updatedEvent)) =>
        vsp.core.CalendarEventService.updateEvent(updatedEvent) match {
          case Right(_) => 
            println("UI: Pomyślnie zaktualizowano dane w bazie.")
            refresh(currentMonth)
          case Left(error) => 
            println(s"UI: Błąd aktualizacji: $error")
            // Możesz tu dodać Alert, jeśli walidacja nie przejdzie
        }
        
      case _ => // Użytkownik zamknął okno lub kliknął Cancel
    }
    
  }*/
  private def handleEventInteraction(event: CalendarEvent, currentMonth: java.time.LocalDate): Unit = {
    val details = new EventDetailsDialog(event)
    
    // showAndWait() zatrzyma wykonanie tego kodu do momentu, 
    // aż użytkownik kliknie "Close Window" lub "Delete"
    details.showAndWait() 

    // Kiedy okno zostanie w końcu zamknięte, odświeżamy cały widok,
    // aby pokazać wszystkie zmiany zapisane w międzyczasie do bazy.
    refresh(currentMonth)
  }


  refresh(initialDate)
}