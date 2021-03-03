import { RestApiService } from './../shared/rest-api.service';
import { Component, OnInit } from '@angular/core';
import { Appointment } from '../shared/rest-api.service';

@Component({
  selector: 'app-list-appointments',
  templateUrl: './list-appointments.component.html',
  styleUrls: ['./list-appointments.component.css']
})
export class ListAppointmentsComponent implements OnInit {
  appointments: Array<Appointment> = [];
  displayedColumns: string[] = ['DateTime', 'Type', 'User', 'Description'];

  constructor(public restApi: RestApiService) {
    this.updateAppointments()
  }

  ngOnInit(): void {
  }

  updateAppointments() {
    this.restApi.getAppointments().subscribe((data: Array<Appointment>) => {
      this.appointments = data
    })
  }

}
