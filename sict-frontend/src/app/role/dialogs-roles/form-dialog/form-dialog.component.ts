import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogContent,
  MatDialogClose,
} from '@angular/material/dialog';
import { RolesService } from '@core/service/role.service';
import {
  UntypedFormControl,
  Validators,
  UntypedFormGroup,
  ReactiveFormsModule,
  FormsModule,
  FormGroup,
  FormBuilder,
} from '@angular/forms';
import { Role } from '@core/models/role.model';
import {
  MAT_DATE_LOCALE,
  MatNativeDateModule,
  MatOptionModule,
} from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatRadioModule } from '@angular/material/radio';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMomentDateModule } from '@angular/material-moment-adapter';

export interface DialogData {
  id: string;
  action: string;
  role: Role;
}

@Component({
  selector: 'app-form-dialog',
  templateUrl: './form-dialog.component.html',
  styleUrl: './form-dialog.component.scss',
  providers: [{ provide: MAT_DATE_LOCALE, useValue: 'en-GB' }],
  imports: [
      MatButtonModule,
      MatIconModule,
      MatDialogContent,
      FormsModule,
      ReactiveFormsModule,
      MatFormFieldModule,
      MatInputModule,
      MatRadioModule,
      MatDatepickerModule,
      MatSelectModule,
      MatOptionModule,
      MatDialogClose,
      MatNativeDateModule,
      MatMomentDateModule,
  ],
})
export class FormDialogComponent {
  action: string;
  dialogTitle: string;
  roleForm: FormGroup;
  role: Role;
  url: string | null = null;
  constructor(
    public dialogRef: MatDialogRef<FormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    public roleService: RolesService,
    private fb: FormBuilder
  ){
    this.action = data.action;
    if(this.action === 'edit') {
      this.dialogTitle =
        data.role.name
      this.role = data.role;
    } else {
      this.dialogTitle = 'New Role';
      this.role = new Role();
    }
    this.roleForm = this.createRoleForm();
  }
  formControl = new UntypedFormControl('', [
    Validators.required,
  ]);
  getErrorMessage() {
    return this.formControl.hasError('required')
      ? 'Required field'
      : this.formControl.hasError('name')
      ? 'Not a valid name'
      : '';
  }
  createRoleForm(): UntypedFormGroup {
    return this.fb.group({
      idRole: [this.role.idRole],
      name: [this.role.name, [Validators.required]],
      description: [this.role.description, [Validators.required]],
      status: [this.role.status, [Validators.required]]
    });
  }
  onNoClick(): void {
    this.dialogRef.close();
  }

  submit() {
    if(this.roleForm.valid) {
      const idData = this.roleForm.get('idRole')?.value;
      const formData = this.roleForm.getRawValue();
      if(this.action == 'edit') {
        this.roleService.update(idData, formData).subscribe({
          next: (reponse) => {
            this.dialogRef.close(reponse);
          },
          error: (error) => {
            console.error('Update Error', error);
          },
        });
      } else {
        this.roleService.add(formData).subscribe({
          next: (response) => {
            this.dialogRef.close(response);
          },
        error: (error) => {
          console.error('Add Error:', error);
          },
        });
      }
    }
  }

  onSelectFile(event: Event) {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files[0]) {
      const reader = new FileReader();

      reader.readAsDataURL(target.files[0]); // read file as data url

      reader.onload = (e) => {
        if (e.target) {
          this.url = e.target.result as string; // Explicitly cast to avoid undefined
        }
      };
    }
  }
}
