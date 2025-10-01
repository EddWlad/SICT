import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../service/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Enviamos la cookie de sesión en TODAS las peticiones
    const reqWithCreds = request.clone({ withCredentials: true });

    // No añadimos Authorization: Bearer ... (cookie HttpOnly ya viaja)
    return next.handle(reqWithCreds);
  }
}