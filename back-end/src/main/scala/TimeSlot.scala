package ch.onedoc

import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeConstants
import com.github.nscala_time.time.OrderingImplicits._

import scala.annotation.tailrec



// Slot in term of period (from the beginning of a day)
case class ScheduleSlot(begin: Period, end: Period) {
  def toTimeSlot(d: DateTime) = {
    val day = d.withTimeAtStartOfDay()
    TimeSlot(day.plus(begin), day.plus(end))
  }
}

// Slot in term of date time in the time
case class TimeSlot(begin: DateTime, end: DateTime)

// Weekly schedules slots group by day of week
case class WeeklyScheduleSlots(slots: Map[Int, List[ScheduleSlot]]) {
  // To time slots (real date time) based on a init date time
  def toTimeSlots(fromDay: DateTime): LazyList[List[TimeSlot]] = {
    LazyList.from(0).map { numDay =>
      // Get start of day
      val day: DateTime = fromDay.plusDays(numDay)
        .withTimeAtStartOfDay()

      // Get schedule slots of the day
      slots.get(day.getDayOfWeek) match {
        case None => List()
        case Some(dayScheduleSlots) => dayScheduleSlots.map(_.toTimeSlot(day))
      }
    }
  }
}

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
        case a :: _ if a.begin >= slot.end =>
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

  // Compute free slots in function of schedule slot (by the beginning of weeks),
  // the booked slots and duration to book
  def computeFreeSlots(weeklyScheduleSlots: WeeklyScheduleSlots,
                       bookedSlots: List[TimeSlot],
                       fromDay: DateTime = DateTime.now(),
                       nbDays: Int,
                       duration: Period): List[DateTime] = {
    // Compute free slots based on weekly schedule slots
    val freeSlots: List[TimeSlot] = weeklyScheduleSlots
      .toTimeSlots(fromDay)
      .take(nbDays)
      .flatten
      .filter(s => s.begin > fromDay)
      .sortBy(_.begin).toList

    // Split free slots and get slots that fits into duration
    splitFreeSlots(freeSlots, bookedSlots, List())
      .flatMap( ts =>
        (ts.begin to ts.end by duration)
          .filter(_ + duration <= ts.end)
      )
  }
}
