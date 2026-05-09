package vsp.util

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

object DateUtils {
  // Pobiera listę dni do wyświetlenia w siatce miesiąca
  def getDaysForMonthGrid(month: LocalDate): Seq[LocalDate] = {
    val firstOfMonth = month.withDayOfMonth(1)
    
    val lastOfMonth = firstOfMonth.`with`(TemporalAdjusters.lastDayOfMonth())
    
    val start = firstOfMonth.`with`(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val end = lastOfMonth.`with`(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
    
    Iterator.iterate(start)(_.plusDays(1))
      .takeWhile(d => !d.isAfter(end))
      .toSeq
  }
}