
package ch.onedoc
import com.github.nscala_time.time.Imports._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.github.nscala_time.time.Imports.{DateTime, Period}
import spray.json.{DeserializationException, JsString, JsValue, JsonFormat}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import scala.io.StdIn

import scala.concurrent.Future

import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

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

case class AppointmentDTO(begin: DateTime, appointmentTypeId: Int, userInformation: UserInformation){
  def toAppointment(l: List[AppointmentType]) = Appointment(begin,
    l.find(_.id == appointmentTypeId).head,
    userInformation
  )
}

object BookingAPI {

  // needed to run the route
  implicit val system = ActorSystem(Behaviors.empty, "AppointmentBooking")
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext = system.executionContext

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
    Appointment(DateTime.parse("2021-03-01T10:00:00"), firstConsultation, maximeInfo),
    Appointment(DateTime.parse("2021-03-01T14:00:00"), followUpConsultation, maximeInfo),
    Appointment(DateTime.parse("2021-03-02T11:00:00"), followUpConsultation, maximeInfo),
  )


  implicit object PeriodJsonFormat extends JsonFormat[Period] {
    // deserialization code
    override def read(json: JsValue): Period = {
      json match {
        case JsString(s) => Period.parse(s)
        case _ => throw new DeserializationException("Impossible to parse date time")
      }
    }

    // serialization code
    override def write(period: Period): JsValue = JsString(period.toString)
  }

  implicit object DateTimeJsonFormat extends JsonFormat[DateTime] {
    // deserialization code
    override def read(json: JsValue): DateTime = {
      json match {
        case JsString(s) => DateTime.parse(s)
        case _ => throw new DeserializationException("Impossible to parse date time")
      }
    }

    // serialization code
    override def write(period: DateTime): JsValue = JsString(period.toString)
  }

  // formats for unmarshalling and marshalling

  implicit val appointmentTypeFormat = jsonFormat3(AppointmentType)
  implicit val userInfoFormat = jsonFormat6(UserInformation)
  implicit val appointmentFormat = jsonFormat3(Appointment)
  implicit val appointmentDTOFormat = jsonFormat3(AppointmentDTO)

  def listAppointments(): Future[List[Appointment]] = Future {
    appointments
  }

  def freeSlots(id: Int): Future[List[DateTime]] = {
    // TODO: parralel
    for {
      scheduleSlotsWeekly <- Future{scheduleSlotsWeekly} // TODO: DB
      appointments <- listAppointments()
      appointmentTypes <- Future{appointmentTypes}
    } yield Booking.freeSlots(
      scheduleSlotsWeekly,
      appointments.map(_.toTimeSlot()),
      DateTime.now(),
      appointmentTypes.find(_.id == id).head.duration
    )
  }

  def addAppointments(appointment: Appointment) = Future {
    appointments = appointments.appended(appointment).sortBy(_.begin)
  }

  def main(args: Array[String]): Unit = {
    val route: Route =
      cors() {
        concat(
          get {
            pathPrefix("appointments") {
              onComplete(listAppointments()) { a => complete(a) }
            }
          },
          get {
            pathPrefix("freeSlots") {
              parameter("id".as[Int]) {
                id: Int => onComplete(freeSlots(id)) { a => complete(a) }
              }
            }
          },
          post {
            pathPrefix("appointment") {
              entity(as[AppointmentDTO]) { appointment =>
                println("Appiintment...")
                onComplete(addAppointments(appointment.toAppointment(appointmentTypes))) {
                  _ => complete("OK")
                }
              }
            }
          }

          // TODO:
          // 1) POST: book a slots...
          // 2) update
          /*
          Exemple:
        post {
          path("create-order") {
            entity(as[Order]) { order =>
              val saved: Future[Done] = saveOrder(order)
              onSuccess(saved) { _ => // we are not interested in the result value `Done` but only in the fact that it was successful
                complete("order created")
              }
            }
          }
        }*/
        )
      }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}