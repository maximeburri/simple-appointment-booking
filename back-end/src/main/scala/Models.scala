package ch.onedoc
import ch.onedoc.BookingAPI.db
import com.github.nscala_time.time.Imports.{DateTime, Period}
import com.github.nscala_time.time.Imports._

import scala.concurrent.{ExecutionContext, Future}

case class AppointmentType(id: Int, name: String, duration: Period)

case class UserInformation(firstName: String,
                           lastName: String,
                           birthDate: String,
                           phone: String,
                           email: String,
                           description: Option[String] = None)

case class Appointment(begin: DateTime, appointmentType: AppointmentType, userInformation: UserInformation) {
  def toTimeSlot() = TimeSlot(begin, begin + appointmentType.duration)
}

// Generic database
trait Database {
  def getScheduleSlots()(implicit ec: ExecutionContext): Future[List[ScheduleSlot]]
  def getAppointmentTypes()(implicit ec: ExecutionContext): Future[List[AppointmentType]]
  def getAppointments()(implicit ec: ExecutionContext): Future[List[Appointment]]
  def addAppointment(appointment: Appointment)(implicit ec: ExecutionContext): Future[Unit]

  // Compute free slots
  def getFreeSlots(appointmentTypeId: Int)(implicit ec: ExecutionContext): Future[List[DateTime]] = {
    val slotsFuture = this.getScheduleSlots()
    val appointmentsFuture = this.getAppointments()
    val appointmentTypesFuture = this.getAppointmentTypes()

    for {
      scheduleSlotsWeekly <- slotsFuture
      appointments <- appointmentsFuture
      appointmentTypes <- appointmentTypesFuture
    } yield Booking.computeFreeSlots(
      scheduleSlotsWeekly,
      appointments.map(_.toTimeSlot()),
      DateTime.now(),
      appointmentTypes.find(_.id == appointmentTypeId).head.duration
    )
  }
}

// Fake database
case class FakeDatabase() extends Database {
  val maximeInfo = UserInformation(
    firstName = "Maxime",
    lastName = "Burri",
    birthDate = "2020-02-25",
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
  val scheduleSlotsWeekly: List[ScheduleSlot] = List(
    // Mon-thursday: 9h - 12h
    List(0.days, 1.days, 2.days, 3.days)
      .map(day => ScheduleSlot(day + 9.hours, day + 12.hours)),

    // Mon-thursday expected wednesday: 14h - 16h
    List(0.days, 1.days, 3.days)
      .map(day => ScheduleSlot(day + 14.hours, day + 16.hours)),

    // Friday
    List(
      ScheduleSlot(4.days + 10.hours, 4.days + 13.hours),
      ScheduleSlot(4.days + 15.hours, 4.days + 17.hours)
    ),

  ).flatten

  var appointments: List[Appointment] = List(
    Appointment(DateTime.now().plus(10.hours.toDuration), firstConsultation, maximeInfo),
    Appointment(DateTime.now().plus(14.hours.toDuration), followUpConsultation, maximeInfo),
    Appointment(DateTime.now().plus(1.days + 10.hours), followUpConsultation, maximeInfo),
  )

  override def addAppointment(appointment: Appointment)(implicit ec: ExecutionContext) = Future {
    appointments = appointments.appended(appointment).sortBy(_.begin)
  }

  override def getScheduleSlots()(implicit ec: ExecutionContext): Future[List[ScheduleSlot]] = Future {
    scheduleSlotsWeekly
  }

  override def getAppointmentTypes()(implicit ec: ExecutionContext): Future[List[AppointmentType]] = Future {
    appointmentTypes
  }

  override def getAppointments()(implicit ec: ExecutionContext): Future[List[Appointment]] = Future {
    appointments
  }
}

// TODO: sql database