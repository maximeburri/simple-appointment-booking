import { RestApiService, Appointment, BookAppointment } from './shared/rest-api.service';
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
    phone: new FormControl('', [Validators.required]),
    description: new FormControl(''),
  });

  user = {firstName: "", lastName: ""};

  constructor(public restApi: RestApiService) {
    this.updateAppointments()
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
          birthDate: this.form.value.birthDate.toString()
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
      this.restApi.getFreeSlots(this.selectedAppointmentType).subscribe((data: Array<String>) => {
        this.freeSlots = data
      })
  }
}
