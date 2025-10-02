import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogTitle,
  MatDialogContent,
  MatDialogActions,
  MatDialogClose,
} from '@angular/material/dialog';
import { RolesService } from '@core/service/role.service';
import { MatButtonModule } from '@angular/material/button';

export interface DialogData {
  idRole: string;
  name: string;
  description: string;
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
  ],
})
export class DeleteDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<DeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
    public roleService: RolesService
  ){}
  
  confirmDelete(): void {
  this.roleService.delete(this.data.idRole).subscribe({
    next: () => this.dialogRef.close(true),   // <-- éxito
    error: (error) => {
      console.error('Delete Error:', error);
      this.dialogRef.close(false);            // <-- opcional: comunica fallo
    },
  });
}
}
