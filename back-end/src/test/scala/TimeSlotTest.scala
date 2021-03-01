package ch.onedoc

import scala.collection.mutable.Stack
import collection.mutable.Stack
import org.scalatest._
import flatspec._
import matchers._
import com.github.nscala_time.time.Imports._

class TimeSlotTest extends AnyFlatSpec with should.Matchers {

  "Free slots" should "list free slots for given duration" in {
    val day = DateTime.parse("2021-03-01")

    // Test with two schedule slot, no booked slots, 30 minutes
    Booking.computeFreeSlots(
      List(
        ScheduleSlot(10.hours, 11.hours),
        ScheduleSlot(14.hours, 15.hours + 30.minutes)
      ),
      List(),
      day,
      30.minutes
    ) should be (
      List(
        day.plus(10.hours.toDuration),
        day.plus(10.hours + 30.minutes.toDuration),
        day.plus(14.hours.toDuration),
        day.plus(14.hours.toDuration + 30.minutes.toDuration),
        day.plus(15.hours.toDuration)
      )
    )

    // Test with two schedule slot (in two days), 2 booked slots, 30 minutes
    Booking.computeFreeSlots(
      List(
        ScheduleSlot(0.day + 10.hours, 0.day + 11.hours), // Monday
        ScheduleSlot(1.day + 13.hours, 1.day + 15.hours + 30.minutes) // Tuesday
      ),
      List(
        TimeSlot(day.plus(10.hours.toDuration), day.plus(10.hours + 30.minutes.toDuration)),
        TimeSlot(day.plus(1.day + 13.hours + 15.minutes), day.plus(1.day + 14.hours + 15.minutes))
      ),
      day,
      30.minutes
    ) should be (List(
      day.plus(10.hours + 30.minutes.toDuration),
      day.plus(1.day + 14.hours + 15.minutes),
      day.plus(1.day + 14.hours + 45.minutes)
    ))
  }


}