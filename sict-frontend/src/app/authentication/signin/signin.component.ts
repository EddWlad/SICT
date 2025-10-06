import { Component} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Validators, FormsModule, ReactiveFormsModule, FormGroup, FormBuilder } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { NgClass } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { finalize } from 'rxjs';
import { AuthService } from '@core/service/auth.service';
@Component({
    selector: 'app-signin',
    templateUrl: './signin.component.html',
    styleUrls: ['./signin.component.scss'],
    imports: [
        RouterLink,
        FormsModule,
        ReactiveFormsModule,
        MatIconModule,
        MatFormFieldModule,
        NgClass,
        MatButtonModule,
    ]
})
export class SigninComponent {
  loginForm: FormGroup;
  loading = false;
  error: string | null = null;
  submitted = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  
  get form() {
    return this.loginForm.controls;
  }

  onSubmit(): void {
    this.error = null;
    this.submitted = true;  

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const email = this.loginForm.get('email')?.value;
    const password = this.loginForm.get('password')?.value;

    this.loading = true;

    this.authService.login(email, password) // debe hacer POST /login con { withCredentials: true }
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: () => {
          // Opcional: precargar info de usuario (cookie ya viene del backend)
          // this.authService.showUserInfo().subscribe(); // si quieres guardar algo en memoria

          // Redirige a tu ruta de inicio (ajusta según tu app)
          this.router.navigate(['/user']);
        },
        error: (err) => {
          // Muestra mensaje en la UI del template (sin cambiar HTML)
          this.error = (err?.error?.message) ? err.error.message : 'Credenciales inválidas';
        }
      });
  }
}
