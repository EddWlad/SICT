import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogTitle,
  MatDialogContent,
  MatDialogActions,
  MatDialogClose,
} from '@angular/material/dialog';

import { MatButtonModule } from '@angular/material/button';
import { UserService } from '@core/service/user.service';

export interface DialogData {
  idUser: string;
  dateCreate: string;
  fullName: string;
  email: string;
  password: string;
  status: number;
}

@Component({
  selector: 'app-delete',
  templateUrl: './delete.component.html',
  styleUrl: './delete.component.scss',
  imports: [
      MatDialogTitle,
      MatDialogContent,
      MatDialogActions,
      MatButtonModule,
      MatDialogClose,
  ]
})
export class DeleteComponent {
  constructor(
    public dialogRef: MatDialogRef<DeleteComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    public userService: UserService
  ) {}
  confirmDelete(): void {
    this.userService.delete(this.data.idUser).subscribe({
      next: (response) => {
        this.dialogRef.close(response);
      },
      error: (error) => {
        console.error('Delete Error:', error);
      },
    });
  }
}
