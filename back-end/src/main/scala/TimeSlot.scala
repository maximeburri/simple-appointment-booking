package ch.onedoc

import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeConstants
import com.github.nscala_time.time.OrderingImplicits._
import scala.annotation.tailrec

// Slot in term of period (e.g. from the beginning of a week)
case class ScheduleSlot(begin: Period, end: Period) {
  def toTimeSlot(d: DateTime) = TimeSlot(d.plus(begin), d.plus(end))
}

// Slot in term of date time
case class TimeSlot(begin: DateTime, end: DateTime)

object Booking {
  // Split free slots into more granular slots depending on booked slots
  // e.g: Free is          |-------| |--------------|
  //      Book is               |--|      |---|
  //      will results in  |----|    |----|   |-----|
  @tailrec
  def splitFreeSlots(freeSlots: List[TimeSlot],
                     bookedSlot: List[TimeSlot],
                     res: List[TimeSlot]): List[TimeSlot] = {
    freeSlots match {
      case Nil => res.reverse
      case slot :: tailSlot => bookedSlot match {
        case Nil => (res.reverse ::: freeSlots)
        case a :: t if a.end <= slot.begin =>
          splitFreeSlots(freeSlots, t, res)
        case a :: t if a.begin >= slot.end =>
          splitFreeSlots(tailSlot, bookedSlot, slot :: res)
        case a :: t if a.begin >= slot.begin && slot.end >= a.end =>
          splitFreeSlots(
            TimeSlot(a.end, slot.end) :: tailSlot,
            t,
            TimeSlot(slot.begin, a.begin) :: res
          )
      }
    }
  }

  // Compute free slots in function of schedule slot (by the beggining of weeks),
  // the booked slots and duration to book
  def computeFreeSlots(scheduleSlotsWeekly: List[ScheduleSlot],
                       bookedSlots: List[TimeSlot],
                       fromDay: DateTime = DateTime.now(),
                       duration: Period): List[DateTime] = {

    // Start of week
    val startWeek = fromDay
      .withDayOfWeek(DateTimeConstants.MONDAY)
      .withTimeAtStartOfDay()

    // Add schedule slot to start of week and filter passed dates
    val scheduleSlots = scheduleSlotsWeekly.map(_.toTimeSlot(startWeek))
      .filter(_.begin > fromDay).sortBy(_.begin)

    // Split free slots and get slots that fits into duration
    splitFreeSlots(scheduleSlots, bookedSlots, List())
      .flatMap( ts =>
        (ts.begin to ts.end by duration)
          .filter(_ + duration <= ts.end)
      )
  }
}
