import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { AdvanceTable } from './advance-table.model';

@Injectable({
  providedIn: 'root',
})
export class AdvanceTableService {
  private readonly API_URL = 'assets/data/advanceTable.json';
  dataChange: BehaviorSubject<AdvanceTable[]> = new BehaviorSubject<
    AdvanceTable[]
  >([]);

  constructor(private httpClient: HttpClient) {}

  /** GET: Fetch all advance tables */
  getAllAdvanceTables(): Observable<AdvanceTable[]> {
    return this.httpClient
      .get<AdvanceTable[]>(this.API_URL)
      .pipe(catchError(this.handleError));
  }

  /** POST: Add a new advance table */
  addAdvanceTable(advanceTable: AdvanceTable): Observable<AdvanceTable> {
    // Add the new advance table to the data array
    return of(advanceTable).pipe(
      map((response) => {
        return response; // Return response (in this case, the advanceTable itself)
      }),
      catchError(this.handleError)
    );

    // API call to add the advance table
    // return this.httpClient.post<AdvanceTable>(this.API_URL, advanceTable).pipe(
    //   map((response) => {
    //     return response; // Return response from API
    //   }),
    //   catchError(this.handleError)
    // );
  }

  /** PUT: Update an existing advance table */
  updateAdvanceTable(advanceTable: AdvanceTable): Observable<AdvanceTable> {
    // Update the advance table in the data array
    return of(advanceTable).pipe(
      map((response) => {
        return response; // Return the updated advanceTable
      }),
      catchError(this.handleError)
    );

    // API call to update the advance table
    // return this.httpClient.put<AdvanceTable>(`${this.API_URL}`, advanceTable).pipe(
    //   map((response) => {
    //     return response; // Return response from API
    //   }),
    //   catchError(this.handleError)
    // );
  }

  /** DELETE: Remove an advance table by ID */
  deleteAdvanceTable(id: number): Observable<number> {
    // Delete the advance table by ID from the data array
    return of(id).pipe(
      map((response) => {
        return id; // Return the ID of the deleted advance table
      }),
      catchError(this.handleError)
    );

    // API call to delete the advance table
    // return this.httpClient.delete<void>(`${this.API_URL}`).pipe(
    //   map((response) => {
    //     return id; // Return the ID of the deleted advance table
    //   }),
    //   catchError(this.handleError)
    // );
  }

  /** Handle Http operation that failed */
  private handleError(error: HttpErrorResponse) {
    console.error('An error occurred:', error.message);
    return throwError(
      () => new Error('Something went wrong; please try again later.')
    );
  }
}
