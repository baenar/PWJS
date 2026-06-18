package vsp.ui.calendar

import scalafx.Includes._
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.input.MouseEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import vsp.model.CalendarEvent
import vsp.core.CalendarEventService
import vsp.ui.dialogs.EventDetailsDialog
import vsp.ui.util.WeatherIconUtil
import scalafx.scene.control.Tooltip

class AgendaView(onEventInteraction: () => Unit) extends VBox {

  // Podstawowa konfiguracja kontenera
  padding = Insets(20)
  spacing = 15
  vgrow = Priority.Always
  hgrow = Priority.Always
  style = "-fx-background-color: white;"

  private val englishLocale = Locale.ENGLISH
  private val dayFormatter = DateTimeFormatter.ofPattern("EEE").withLocale(englishLocale) // np. "Mon"
  private val numFormatter = DateTimeFormatter.ofPattern("d").withLocale(englishLocale)   // np. "15"
  private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(englishLocale) // np. "May 2026"

  // Główny kontener na listę dni, który będzie scrollowany
  private val contentBox = new VBox {
    spacing = 20
    padding = Insets(0, 15, 0, 0)
    maxWidth = Double.MaxValue
  }

  private val scrollPane = new ScrollPane {
    content = contentBox
    fitToWidth = true
    vgrow = Priority.Always
    style = "-fx-background-color: transparent; -fx-background: white;"
  }

  children = Seq(scrollPane)

  // --- LOGIKA WIDOKU ---
  def refresh(): Unit = {
    contentBox.children.clear()

    // 1. Pobieramy i filtrujemy wydarzenia (Tylko dzisiaj i przyszłość)
    val today = LocalDate.now()
    val allEvents = CalendarEventService.getAllEvents()
    val upcomingEvents = allEvents.filter { event =>
      !event.startTime.toLocalDate.isBefore(today)
    }

    if (upcomingEvents.isEmpty) {
      // Widok pustej agendy
      contentBox.children = Seq(
        new VBox {
          alignment = Pos.Center
          padding = Insets(50)
          spacing = 10
          children = Seq(
            new Label("No upcoming events") { style = "-fx-font-size: 18px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;" },
            new Label("Click a day in the calendar to add an event") { style = "-fx-font-size: 14px; -fx-text-fill: #bdc3c7;" }
          )
        }
      )
      return
    }

    // 2. Grupowanie wydarzeń po dacie (odpowiednik reduce w TS)
    val eventsByDate: Map[LocalDate, List[CalendarEvent]] = upcomingEvents.groupBy(_.startTime.toLocalDate)

    // 3. Sortowanie dat rosnąco
    val sortedDates = eventsByDate.keys.toList.sorted

    // 4. Budowanie widoku dla każdego dnia
    sortedDates.foreach { date =>
      val dayEvents = eventsByDate(date).sortBy(_.startTime)
      val isToday = (date == today)

      // --- NAGŁÓWEK DNIA (Sticky Header z TS) ---
      val dayLabel = new VBox {
        alignment = Pos.Center
        padding = Insets(5, 10, 5, 10)
        style = if (isToday) "-fx-background-color: #3498db; -fx-background-radius: 8;"
                else "-fx-background-color: #ecf0f1; -fx-background-radius: 8;"
        
        children = Seq(
          new Label(date.format(dayFormatter)) { 
            style = s"-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: ${if (isToday) "white" else "#7f8c8d"};" 
          },
          new Label(date.format(numFormatter)) { 
            style = s"-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: ${if (isToday) "white" else "#2c3e50"};" 
          }
        )
      }

      val monthYearLabel = new VBox {
        alignment = Pos.CenterLeft
        children = Seq(
          new Label(date.format(monthYearFormatter)) { style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;" },
          new Label(s"${dayEvents.size} event${if(dayEvents.size > 1) "s" else ""}") { style = "-fx-font-size: 12px; -fx-text-fill: #95a5a6;" }
        )
      }

      val header = new HBox(15) {
        alignment = Pos.CenterLeft
        padding = Insets(0, 0, 10, 0)
        style = "-fx-border-color: #ecf0f1; -fx-border-width: 0 0 1 0; -fx-border-style: solid;"
        children = Seq(dayLabel, monthYearLabel)
      }

      // --- KARTY WYDARZEŃ DLA DANEGO DNIA ---
      // --- KARTY WYDARZEŃ DLA DANEGO DNIA ---
      val eventsList = new VBox(10) {
        padding = Insets(10, 0, 20, 10) // Lekkie wcięcie listy pod nagłówkiem
      }

      // Przygotowujemy ładny format czasu (żeby uniknąć pokazywania nanosekund)
      val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

      dayEvents.foreach { event =>
        
        // 1. Sprawdzamy i budujemy element pogodowy dla Agendy
        val weatherInfo: Seq[scalafx.scene.Node] = event.weather match {
          case Some(w) =>
            val iconView = WeatherIconUtil.getWeatherIcon(w, 24.0) 
            val tempString = event.temperature.map(t => f"$t%.1f°C").getOrElse("")
            
            val weatherTooltip = new Tooltip {
              text = s"Weather: $w"
              style = "-fx-font-size: 13px; -fx-background-color: rgba(17, 24, 39, 0.9);"
            }
            
            val iconLabel = new Label {
              graphic = iconView
              tooltip = weatherTooltip
            }

            Seq(
              new Label(" ") { style = "-fx-padding: 0 5 0 5;" }, // Większy odstęp w Agendzie
              iconLabel,
              new Label(tempString) { 
                style = "-fx-font-size: 12px; -fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-padding: 0 0 0 4;" 
                tooltip = weatherTooltip
              }
            )
          case None => 
            Seq.empty 
        }

        // 2. Budujemy główną kartę
        val eventCard = new VBox(5) {
          padding = Insets(15)
          style = """
            -fx-background-color: #fcfcfc; 
            -fx-background-radius: 8; 
            -fx-border-color: #3498db; 
            -fx-border-width: 0 0 0 4; 
            -fx-cursor: hand;
          """
          
          // Efekt Hover
          onMouseEntered = _ => style = style.value.replace("-fx-background-color: #fcfcfc;", "-fx-background-color: #f0f8ff;")
          onMouseExited = _ => style = style.value.replace("-fx-background-color: #f0f8ff;", "-fx-background-color: #fcfcfc;")

          onMouseClicked = (e: MouseEvent) => {
            val dialog = new EventDetailsDialog(event, () => {
              refresh() 
              onEventInteraction() 
            })
            dialog.showAndWait()
          }

          children = Seq(
            new Label(event.title) { style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;" },
            
            // Pasek ze szczegółami z doklejoną POGODĄ
            new HBox(15) {
              alignment = Pos.CenterLeft
              children = Seq(
                new Label(s"🕒 ${event.startTime.format(timeFormatter)} - ${event.endTime.format(timeFormatter)}") { 
                  style = "-fx-text-fill: #7f8c8d; -fx-font-size: 12px;" 
                },
                new Label(s"📍 ${event.city.name}") { 
                  style = "-fx-text-fill: #7f8c8d; -fx-font-size: 12px;" 
                }
              ) ++ weatherInfo // <-- Doklejamy przygotowaną pogodę!
            },
            
            if (event.description.nonEmpty) 
              new Label(event.description) { 
                style = "-fx-text-fill: #95a5a6; -fx-font-size: 12px; -fx-padding: 5 0 0 0;"
                wrapText = true
              } 
            else new Region()
          )
        }
        eventsList.children.add(eventCard)
      }

      // Łączymy nagłówek dnia z jego wydarzeniami
      contentBox.children.addAll(header, eventsList)
    }
  }

  // Uruchomienie przy pierwszej inicjalizacji
  refresh()
}