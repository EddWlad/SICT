import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogContent,
  MatDialogClose,
} from '@angular/material/dialog';
import {
  UntypedFormControl,
  Validators,
  UntypedFormGroup,
  ReactiveFormsModule,
  FormsModule,
  FormGroup,
  FormBuilder,
} from '@angular/forms';
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
import { UserListRoleDTOI } from '@core/models/user-list-role-DTOI';
import { Role } from '@core/models/role.model';
import { UserService } from '@core/service/user.service';
import { RolesService } from '@core/service/role.service';
import { User } from '@core/models/user.model';

export interface DialogData {
  id: number;
  action: string;
  userListRole: UserListRoleDTOI;
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
  userListRoleForm: FormGroup;
  userListRole: UserListRoleDTOI;
  url: string | null = null;

  role: Role[];

  hide = true;

  fileName: string = '';

  selectedFiles: FileList;

constructor(
  public dialogRef: MatDialogRef<FormDialogComponent>,
  @Inject(MAT_DIALOG_DATA) public data: any,
  public userService: UserService,
  private fb: FormBuilder,
  private roleService: RolesService
) {
  this.action = data?.action;

  // Inicialización segura
  this.userListRole = {
    user: new User(),
    roles: []
  };

  if (this.action === 'edit' && data?.userListRole?.user) {
    this.dialogTitle = data.userListRole.user.name;
    this.userListRole = data.userListRole;
    /*if (this.userListRole.user.files) {
      this.url = this.userListRole.user.files;
      this.fileName = this.userListRole.user.files;
    }*/
  } else {
    this.dialogTitle = 'New User';
    this.userListRole.user = new User();
  }

  // Crear formulario con valores cargados o por defecto
  this.userListRoleForm = this.createUserForm();
}


  ngOnInit() {
    this.roleService.findAll().subscribe((data) => {
      this.role = data;
    });
  }

  compareRole(a: Role, b: Role): boolean {
    return a && b ? a.idRole === b.idRole : a === b;
  }

  formControl = new UntypedFormControl('', [
    Validators.required,
    // Validators.email,
  ]);
  getErrorMessage() {
    return this.formControl.hasError('required')
      ? 'Required field'
      : this.formControl.hasError('email')
      ? 'Not a valid email'
      : '';
  }
  createUserForm(): UntypedFormGroup {
    return this.fb.group({
      idUser: [this.userListRole.user.idUser],
      dateCreate: [
        this.userListRole.user.dateCreate ?? new Date(), // ← aquí usamos la fecha actual si es nuevo
        [Validators.required],
      ],
      fullName: [this.userListRole.user.fullName, [Validators.required]],
      email: [this.userListRole.user.email, [Validators.required, Validators.email]],
      password: [this.userListRole.user.password, [Validators.required]],
      status: [this.userListRole.user.status, [Validators.required]],
      role: [this.userListRole.roles?.[0] ?? null, [Validators.required]]
    });
  }
  onNoClick(): void {
    this.dialogRef.close();
  }

submit(): void {
  if (this.userListRoleForm.valid) {
    const idData = this.userListRoleForm.get('idUser')?.value;

    const enviarDatos = (urlList: string[] = []) => {
      // Si se subió imagen, la asignamos
      if (urlList.length > 0) {
        this.userListRoleForm.get('files')?.setValue(urlList[0]);
      }

      const formValue = this.userListRoleForm.getRawValue();

      const dateWithoutZ = new Date(formValue.dateCreate).toISOString().slice(0, 19);

      const dto: UserListRoleDTOI = {
        user: {
          idUser: idData ?? null,
          fullName: formValue.fullName,
          email: formValue.email,
          password: formValue.password,
          status: formValue.status,
          dateCreate: dateWithoutZ,
        },
        roles: [
          {
            idRole: formValue.role?.idRole ?? formValue.role?.id ?? null,
            name: formValue.role?.name ?? '',
            description: formValue.role?.description ?? '',
            status: formValue.role?.status ?? 1
          }
        ]
      };

      if (this.action === 'edit') {
        this.userService.updateTransactional(idData, dto).subscribe({
          next: (response) => this.dialogRef.close(response),
          error: (error) => console.error('Update Error:', error),
        });
      } else {
        this.userService.saveTransactional(dto).subscribe({
          next: (response) => this.dialogRef.close(response),
          error: (error) => console.error('Add Error:', error),
        });
      }
    };

    // Subida de imagen (opcional)
    if (this.selectedFiles && this.selectedFiles.length > 0) {
      this.userService.saveFile(this.selectedFiles.item(0)).subscribe({
        next: (urlList: string[]) => enviarDatos(urlList),
        error: (error) => {
          console.error('Upload Error:', error);
          enviarDatos(); // Continuar sin imagen
        }
      });
    } else {
      enviarDatos(); // Sin imagen
    }
  }
}

  onSelectFile(event: any) {

    this.fileName = event.target.files[0]?.name;
    this.selectedFiles = event.target.files;
  }

  upload(){
    this.userService.saveFile(this.selectedFiles.item(0)).subscribe();
  }
}
