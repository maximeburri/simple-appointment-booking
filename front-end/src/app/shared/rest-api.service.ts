import { getLocaleDateTimeFormat } from '@angular/common';
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

export type FreeSlots = Map<string, Array<Date>>

@Injectable({
  providedIn: 'root'
})
export class RestApiService {
  apiURL = 'http://localhost:8080'; //TODO: move in env vars
  constructor(private http: HttpClient) { }

  getAppointments(): Observable<Array<Appointment>> {
    return this.http.get<Array<Appointment>>(this.apiURL + '/appointments')
  }

  getFreeSlots(id: number, numPage: number): Observable<FreeSlots> {
    return this.http.get<FreeSlots>(this.apiURL + '/freeSlots?id=' + id.toString() + '&page=' + numPage.toString())
  }
  
  getAppointmentTypes(): Observable<Array<AppointmentType>> {
    return this.http.get<Array<AppointmentType>>(this.apiURL + '/appointmentTypes')
  }

  addAppointment(appointment: BookAppointment): Observable<any> {
    return this.http.post<any>(this.apiURL + '/appointment', appointment, { responseType: 'text' as 'json' })
  }

}
