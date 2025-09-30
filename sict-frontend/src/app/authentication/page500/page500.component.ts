import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
@Component({
    selector: 'app-page500',
    templateUrl: './page500.component.html',
    styleUrls: ['./page500.component.scss'],
    imports: [FormsModule, MatButtonModule]
})
export class Page500Component {
  constructor(private router: Router) {}

  submit() {
    this.router.navigate(['/authentication/signin']);
  }
}
