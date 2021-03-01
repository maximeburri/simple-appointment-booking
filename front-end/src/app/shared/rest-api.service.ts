import { HttpClient, HttpHeaderResponse, HttpHeaders } from '@angular/common/http';
import { StringMap } from '@angular/compiler/src/compiler_facade_interface';
import { Injectable, Optional } from '@angular/core';
import { Observable } from 'rxjs';
import { retry, catchError } from 'rxjs/operators';

export interface Appointment {
  begin: String,
  appointmentType: AppointmentType,
  userInformation: UserInformation,
}
export interface AppointmentType {
  id: number,
  name: String,
  duration: String,
}

export interface BookAppointment {
  begin: String,
  appointmentTypeId: number,
  userInformation: UserInformation,
}

export interface UserInformation {
  firstName: String,
  lastName: String,
  birthDate: String,
  phone: String,
  email: String,
  description: String | undefined
}

export interface AppointmentType { 
  id: number,
  name: String, 
  duration: String
}

@Injectable({
  providedIn: 'root'
})
export class RestApiService {
  apiURL = 'http://localhost:8080';
  constructor(private http: HttpClient) { }


  // Http Options
  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json'
    })
  }

  // HttpClient API get() method => Fetch employee
  getAppointments(): Observable<Array<Appointment>> {
    return this.http.get<Array<Appointment>>(this.apiURL + '/appointments')
  }

  getFreeSlots(id: number): Observable<Array<String>> {
    return this.http.get<Array<String>>(this.apiURL + '/freeSlots?id=' + id.toString())
  }
  
  getAppointmentTypes(): Observable<Array<AppointmentType>> {
    return this.http.get<Array<AppointmentType>>(this.apiURL + '/appointmentTypes')
  }

  addAppointment(appointment: BookAppointment): Observable<any> {
    return this.http.post<any>(this.apiURL + '/appointment', appointment, { responseType: 'text' as 'json' })
  }

}
