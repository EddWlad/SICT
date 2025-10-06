import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Inject, inject, Injectable } from '@angular/core';
import { catchError, map, Observable, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GenericService<T> {
  constructor(
    protected http: HttpClient,
    @Inject('API_URL') private url: string
  ) { }

  findAll(): Observable<T[]> {
    return this.http
      .get<T[]>(this.url)
      .pipe(catchError(this.handleError));
  }

  findByid(id: string): Observable<T> {
    return this.http
    .get<T>(`${this.url}/${id}`)
    .pipe(catchError(this.handleError));
  }

  add(t: T): Observable<T>{
    return this.http.post<T>(this.url, t).pipe(
      map((response) => {
        return t;
      }),
      catchError(this.handleError)
    );
  }

  update(id: string, t: T): Observable<T>{
    return this.http
    .put(`${this.url}/${id}`, t).pipe(
      map((response) => {
        return t; // return response from API
      }),
      catchError(this.handleError)
    );
  }

  delete(id: string): Observable<string> {
    return this.http.delete<void>(`${this.url}/${id}`).pipe(
      map((response) => {
        return id; // return response from API
      }),
      catchError(this.handleError)
    );
  }


  private handleError(error: HttpErrorResponse) {
    // Customize this method based on your needs
    console.error('An error occurred:', error.message);
    return throwError(
      () => new Error('Something went wrong; please try again later.')
    );
  }
}