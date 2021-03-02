package ch.burri

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import org.joda.time.DateTime

import scala.util.control.NonFatal

case class BookAppointmentDTO(begin: DateTime, appointmentTypeId: Int, userInformation: UserInformation){
  def toAppointment(l: List[AppointmentType]) = Appointment(begin,
    l.find(_.id == appointmentTypeId).head,
    userInformation
  )
}

object BookingAPI {
  import ModelJsonFormats._

  import akka.http.scaladsl.server._
  import Directives._

  implicit val system = ActorSystem(Behaviors.empty, "AppointmentBooking")
  implicit val executionContext = system.executionContext
  implicit val bookAppointmentFormat = jsonFormat3(BookAppointmentDTO)

  val db = FakeDatabase()

  def main(args: Array[String]): Unit = {
    val route: Route =
      cors() { // To disable in production
        get {
          pathPrefix("appointments") {
            onComplete(db.getAppointments()) { a => complete(a) }
          }
        } ~
        get {
          pathPrefix("freeSlots") {
            parameters("id".as[Int], "page".as[Int].withDefault(0)) {
              (id: Int, page: Int) => {
                val nbDays = 7
                val fromDay = {
                  if(page <= 0) DateTime.now()
                  else DateTime.now().withTimeAtStartOfDay().plusDays(page * nbDays)
                }
                val freeSlots = db.getFreeSlots(id, fromDay, nbDays)
                  .map(_.groupBy(_.withTimeAtStartOfDay()))

                onComplete(freeSlots) { a =>
                  complete(a)
                }
              }
            }
          }
        } ~
        get {
          pathPrefix("appointmentTypes") {
            onComplete(db.getAppointmentTypes()) { a => complete(a) }
          }
        } ~
        post {
          pathPrefix("appointment") {
            entity(as[BookAppointmentDTO]) { bookAppointment =>
              onComplete(for {
                types <- db.getAppointmentTypes()
                res <- db.addAppointment(bookAppointment.toAppointment(types))
              } yield res) {
                _ => complete("OK")
              }
            }
          }
        }
      }

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}