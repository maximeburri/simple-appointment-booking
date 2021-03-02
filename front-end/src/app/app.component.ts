import { RestApiService, Appointment, BookAppointment, AppointmentType } from './shared/rest-api.service';
import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { _YAxis } from '@angular/cdk/scrolling';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  title = 'booking';
  selectedAppointmentType : number | undefined = undefined;
  appointmentTypes: Array<AppointmentType> = []; // TODO: from api

  appointments: Array<Appointment> = [];
  displayedColumns: string[] = ['DateTime', 'Type', 'User', 'Description'];
  freeSlots: Array<Date> = [];

  form = new FormGroup({
    appointmentDatetime: new FormControl('', [Validators.required]),
    firstName: new FormControl('', [Validators.required]),
    lastName: new FormControl('', [Validators.required]),
    email: new FormControl('', [Validators.required]),
    birthDate: new FormControl('', [Validators.required]),
    phone: new FormControl('', [Validators.required]),
    description: new FormControl(''),
  });

  user = {firstName: "", lastName: ""};

  constructor(public restApi: RestApiService) {
    this.updateAppointments()
    this.updateAppointmentTypes();
  }

  onSubmit() {
    console.log("Submit");
    console.log(this.form.value);

    if(this.selectedAppointmentType !== undefined){
      let bookAppointment: BookAppointment = {
        begin: this.form.controls.appointmentDatetime.value,
        appointmentTypeId: this.selectedAppointmentType,
        userInformation: {
          ...this.form.value,
          birthDate: this.form.value.birthDate.toISOString().slice(0, 10)
        }
      }
      console.log(bookAppointment)

      this.restApi.addAppointment(bookAppointment).subscribe(() => {
        this.selectedAppointmentType = undefined;
        this.freeSlots = [];
        this.updateAppointments();
        this.form.reset();
      });
    }
  }

  updateAppointments() {
    this.form.controls.appointmentDatetime.setValue(undefined);
    this.restApi.getAppointments().subscribe((data: Array<Appointment>) => {
      this.appointments = data
    })
  }

  updateFreeSlots() {
    if(this.selectedAppointmentType !== undefined)
      this.restApi.getFreeSlots(this.selectedAppointmentType).subscribe((data: Array<Date>) => {
        this.freeSlots = data
      })
  }

  updateAppointmentTypes() {
    this.restApi.getAppointmentTypes().subscribe((data) => {
      this.appointmentTypes = data;
    })
  }
}
