package ch.onedoc

import com.github.nscala_time.time.Imports.{DateTime, Period}
import spray.json.DefaultJsonProtocol.{jsonFormat3, jsonFormat6}
import spray.json.{DeserializationException, JsString, JsValue, JsonFormat}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

object ModelJsonFormats {
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
}
