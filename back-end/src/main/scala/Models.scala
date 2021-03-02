package ch.onedoc
import com.github.nscala_time.time.Imports.{DateTime, Period}
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeConstants

import scala.concurrent.{ExecutionContext, Future}

case class AppointmentType(id: Int, name: String, duration: Period)

case class UserInformation(firstName: String,
                           lastName: String,
                           birthDate: DateTime,
                           phone: String,
                           email: String,
                           description: Option[String] = None)

case class Appointment(begin: DateTime, appointmentType: AppointmentType, userInformation: UserInformation) {
  def toTimeSlot() = TimeSlot(begin, begin + appointmentType.duration)
}

// Generic database
trait Database {
  def getScheduleSlots()(implicit ec: ExecutionContext): Future[WeeklyScheduleSlots]
  def getAppointmentTypes()(implicit ec: ExecutionContext): Future[List[AppointmentType]]
  def getAppointments()(implicit ec: ExecutionContext): Future[List[Appointment]]
  def addAppointment(appointment: Appointment)(implicit ec: ExecutionContext): Future[Unit]

  // Compute free slots
  def getFreeSlots(appointmentTypeId: Int, fromDay: DateTime, nbDays: Int)(implicit ec: ExecutionContext): Future[List[DateTime]] = {
    val slotsFuture = this.getScheduleSlots()
    val appointmentsFuture = this.getAppointments()
    val appointmentTypesFuture = this.getAppointmentTypes()

    for {
      weeklyScheduleSlots <- slotsFuture
      appointments <- appointmentsFuture
      appointmentTypes <- appointmentTypesFuture
    } yield Booking.computeFreeSlots(
      weeklyScheduleSlots,
      appointments.map(_.toTimeSlot()),
      fromDay,
      nbDays,
      appointmentTypes.find(_.id == appointmentTypeId).head.duration

    )
  }
}

// Fake database
case class FakeDatabase() extends Database {
  val maximeInfo = UserInformation(
    firstName = "Maxime",
    lastName = "Burri",
    birthDate = DateTime.parse("1995-02-25"),
    phone = "07655544433",
    email = "max@bu.ch",
    description = None
  )

  val firstConsultation = AppointmentType(0, "First consultation", 45.minutes)
  val followUpConsultation = AppointmentType(1, "Follow-up consultation", 30.minutes)

  val appointmentTypes: List[AppointmentType] = List(
    firstConsultation,
    followUpConsultation
  )

  // Schedule slots
  val weeklyScheduleSlots = WeeklyScheduleSlots(Map(
    DateTimeConstants.MONDAY -> List(
      ScheduleSlot(9.hours, 12.hours),
      ScheduleSlot(14.hours, 16.hours)
    ),
    DateTimeConstants.TUESDAY -> List(
      ScheduleSlot(9.hours, 12.hours),
      ScheduleSlot(14.hours, 16.hours)
    ),
    DateTimeConstants.WEDNESDAY -> List(
      ScheduleSlot(9.hours, 12.hours)
    ),
    DateTimeConstants.THURSDAY -> List(
      ScheduleSlot(9.hours, 12.hours),
      ScheduleSlot(14.hours, 16.hours)
    ),
    DateTimeConstants.FRIDAY -> List(
      ScheduleSlot(10.hours, 13.hours),
      ScheduleSlot(15.hours, 17.hours)
    )
  ))

  var appointments: List[Appointment] = List()

  override def addAppointment(appointment: Appointment)(implicit ec: ExecutionContext) = Future {
    appointments = appointments.appended(appointment).sortBy(_.begin)
  }

  override def getScheduleSlots()(implicit ec: ExecutionContext): Future[WeeklyScheduleSlots] = Future {
    weeklyScheduleSlots
  }

  override def getAppointmentTypes()(implicit ec: ExecutionContext): Future[List[AppointmentType]] = Future {
    appointmentTypes
  }

  override def getAppointments()(implicit ec: ExecutionContext): Future[List[Appointment]] = Future {
    appointments
  }
}

// TODO: sql database