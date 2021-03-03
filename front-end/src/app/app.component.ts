import { ListAppointmentsComponent } from './list-appointments/list-appointments.component';
import { Component, ViewChild } from '@angular/core';

import { _YAxis } from '@angular/cdk/scrolling';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  @ViewChild('listapp') listapp!:ListAppointmentsComponent;
  
  constructor() {

  }
}
