import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { RestApiService, BookAppointment, AppointmentType, FreeSlots } from '../shared/rest-api.service';

@Component({
  selector: 'app-add-appointment',
  templateUrl: './add-appointment.component.html',
  styleUrls: ['./add-appointment.component.css']
})
export class AddAppointmentComponent implements OnInit {
  @Output() updateList: EventEmitter<any> = new EventEmitter();

  selectedAppointmentType : number | undefined = undefined;
  appointmentTypes: Array<AppointmentType> = []; // TODO: from api

  freeSlots: FreeSlots = new Map();
  freeSlotsPage: number = 0;

  form = new FormGroup({
    appointmentDatetime: new FormControl('', [Validators.required]),
    firstName: new FormControl('', [Validators.required]),
    lastName: new FormControl('', [Validators.required]),
    email: new FormControl('', [Validators.required]),
    birthDate: new FormControl('', [Validators.required]),
    phone: new FormControl('', [Validators.required]),
    description: new FormControl(''),
  });


  constructor(public restApi: RestApiService) {
    this.updateAppointmentTypes();
  }
  
  ngOnInit(): void {
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

      this.restApi.addAppointment(bookAppointment).subscribe(() => {
        this.selectedAppointmentType = undefined;
        this.freeSlots = new Map();
        this.updateList.emit();
        this.form.reset();
        this.freeSlotsPage = 0;
      });
    }
  }

  nextSlots() {
    this.freeSlotsPage++;
    this.updateFreeSlots();
  }

  previousSlots() {
    this.freeSlotsPage--;
    this.updateFreeSlots();
  }

  updateFreeSlots() {
    if(this.selectedAppointmentType !== undefined)
      this.restApi.getFreeSlots(this.selectedAppointmentType, this.freeSlotsPage).subscribe((data) => {
        this.freeSlots = data
      })
  }

  updateAppointmentTypes() {
    this.restApi.getAppointmentTypes().subscribe((data) => {
      this.appointmentTypes = data;
    })
  }

}
