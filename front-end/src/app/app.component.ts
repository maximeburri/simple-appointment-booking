import { RestApiService, Appointment } from './shared/rest-api.service';
import { _YAxis } from '@angular/cdk/scrolling';
import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  title = 'booking';
  selectedAppointmentType : number | undefined = undefined;
  types = [{title:"First consultation", id: 0}, {title: "Follow-up consultation", id: 1}]; // TODO: from api

  appointments: Array<Appointment> = [];
  displayedColumns: string[] = ['DateTime', 'Type', 'User', 'Description'];
  freeSlots: Array<String> = [];

  form = new FormGroup({
    appointmentDatetime: new FormControl('', [Validators.required]),
    firstName: new FormControl('', [Validators.required]),
    lastName: new FormControl('', [Validators.required]),
    email: new FormControl('', [Validators.required]),
    birthDate: new FormControl('', [Validators.required]),
    phoneNumber: new FormControl('', [Validators.required]),
    description: new FormControl(''),
  });

  user = {firstName: "", lastName: ""};

  constructor(public restApi: RestApiService) {
    this.updateAppointments()
  }

  onSubmit() {
    console.log("Submit");
    this.updateAppointments();
    this.form.reset();
  }

  updateAppointments() {
    this.form.controls.appointmentDatetime.setValue(undefined);
    this.restApi.getAppointments().subscribe((data: Array<Appointment>) => {
      this.appointments = data
    })
  }


  updateFreeSlots() {
    if(this.selectedAppointmentType !== undefined)
      this.restApi.getFreeSlots(this.selectedAppointmentType).subscribe((data: Array<String>) => {
        this.freeSlots = data
      })
  }
}
