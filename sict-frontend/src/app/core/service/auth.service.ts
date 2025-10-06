import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { tap, mapTo} from 'rxjs';
import { environment } from 'environments/environment.development';
import { Router } from '@angular/router';

interface ILoginRequest {
  email: string;
  password: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private url: string = `${environment.HOST}/login`;
  private http = inject(HttpClient);
  private router = inject(Router);

  // ✅ IMPORTANTE: withCredentials para que el navegador guarde la cookie de sesión
  login(email: string, password: string) {
    const body: ILoginRequest = { email, password };
    return this.http.post<any>(this.url, body, { withCredentials: true });
  }

  showUserInfo() {
    return this.http.get<{ fullName: string }>(`${environment.HOST}/auth/user`, { withCredentials: true });
  }

// ✅ ahora devuelve Observable<void> (no se suscribe adentro)
  logout() {
    return this.http
      .get(`${environment.HOST}/auth/logout`, { withCredentials: true })
      .pipe(
        tap(() => this.router.navigate(['/authentication/signin'])),
        mapTo(void 0)
      );
  }
  /*private currentUserSubject: BehaviorSubject<User>;
  public currentUser: Observable<User>;

  private users = [
    {
      id: 1,
      username: 'admin@lorax.com',
      password: 'admin',
      firstName: 'Sarah',
      lastName: 'Smith',
      token: 'admin-token',
    },
  ];

  constructor(private http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<User>(
      JSON.parse(localStorage.getItem('currentUser') || '{}')
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User {
    return this.currentUserSubject.value;
  }

  login(username: string, password: string) {

    const user = this.users.find((u) => u.username === username && u.password === password);

    if (!user) {
      return this.error('Username or password is incorrect');
    } else {
      localStorage.setItem('currentUser', JSON.stringify(user));
      this.currentUserSubject.next(user);
      return this.ok({
        id: user.id,
        username: user.username,
        firstName: user.firstName,
        lastName: user.lastName,
        token: user.token,
      });
    }
  }
  ok(body?: {
    id: number;
    username: string;
    firstName: string;
    lastName: string;
    token: string;
  }) {
    return of(new HttpResponse({ status: 200, body }));
  }
  error(message: string) {
    return throwError(message);
  }

  logout() {
    // remove user from local storage to log user out
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(this.currentUserValue);
    return of({ success: false });
  }*/
}
