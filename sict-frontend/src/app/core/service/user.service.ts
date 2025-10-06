import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'environments/environment.development';
import { GenericService } from './generic.service';
import { UserListRoleDTOI } from '@core/models/user-list-role-DTOI';
import { User } from '@core/models/user.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService extends GenericService<User> {
  constructor() {
    super(
      inject(HttpClient),
      `${environment.HOST}/users`
    );
  }

  saveTransactional(dto: UserListRoleDTOI){
    return this.http.post(`${environment.HOST}/users`, dto);
  }

  updateTransactional(id: string, dto: UserListRoleDTOI){
    return this.http.put(`${environment.HOST}/users/${id}`, dto);
  }

  getUserByRolAdmins(){
    return this.http.get<User[]>(`${environment.HOST}/users/admins`);
  }

  getUserByRolOperators(){
    return this.http.get<User[]>(`${environment.HOST}/users/operators`);
  }

  findById(id: string): Observable<UserListRoleDTOI> {
    return this.http.get<UserListRoleDTOI>(`${environment.HOST}/users/${id}`);
  }

  saveFile(data: File) {
    const formData: FormData = new FormData();
    formData.append('file', data);
    return this.http.post(`${environment.HOST}/legal-cases/upload`, formData);
  }
}
