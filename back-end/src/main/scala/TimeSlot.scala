package ch.onedoc

import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeConstants
import com.github.nscala_time.time.OrderingImplicits._
import scala.annotation.tailrec

// Slot in term of period
case class ScheduleSlot(begin: Period, end: Period) {
  def toTimeSlot(d: DateTime) = TimeSlot(d.plus(begin), d.plus(end))
}

// Slot in term of date time
case class TimeSlot(begin: DateTime, end: DateTime)

object Booking {

  @tailrec
  def splitFreeSlots(freeSlots: List[TimeSlot], appointments: List[TimeSlot], res: List[TimeSlot]): List[TimeSlot] = {
    freeSlots match {
      case Nil => res.reverse
      case slot :: tailSlot => appointments match {
        case Nil => (res.reverse ::: freeSlots)
        case a :: t if a.end <= slot.begin =>
          splitFreeSlots(freeSlots, t, res)
        case a :: t if a.begin >= slot.end =>
          splitFreeSlots(tailSlot, appointments, slot :: res)
        case a :: t if a.begin >= slot.begin && slot.end >= a.end =>
          splitFreeSlots(
            TimeSlot(a.end, slot.end) :: tailSlot,
            t,
            TimeSlot(slot.begin, a.begin) :: res
          )
      }
    }
  }

  def freeSlots(scheduleSlotsWeekly: List[ScheduleSlot],
                bookedSlots: List[TimeSlot],
                fromDay: DateTime = DateTime.now(),
                duration: Period): List[DateTime] = {

    val startWeek = fromDay
      .withDayOfWeek(DateTimeConstants.MONDAY)
      .withTimeAtStartOfDay()

    val scheduleSlots = scheduleSlotsWeekly.map(_.toTimeSlot(startWeek))
      .filter(_.begin > fromDay).sortBy(_.begin) // TODO: sorting once before..

    splitFreeSlots(scheduleSlots, bookedSlots, List())
      .flatMap( ts =>
        (ts.begin to ts.end by duration)
          .filter(_ + duration <= ts.end)
      )
  }
}
