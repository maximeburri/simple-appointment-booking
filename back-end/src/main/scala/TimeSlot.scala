package ch.burri

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
      case slot :: tailFreeSlots => bookedSlot match {
          // End
        case Nil => (res.reverse ::: freeSlots)
          // Booked slot begin is passed with current slot
        case h :: t if h.end <= slot.begin =>
          splitFreeSlots(freeSlots, t, res) // next booked slot
          // Booked slot after is passed with current slot
        case h :: _ if h.begin >= slot.end =>
          splitFreeSlots(tailFreeSlots, bookedSlot, slot :: res) // next free slot
          // In the current slot: split free slot in two
        case h :: t =>
          splitFreeSlots(
            TimeSlot(
              if (h.end < slot.end) h.end else slot.end,
              slot.end) :: tailFreeSlots,
            t,
            TimeSlot(slot.begin, h.begin) :: res
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
