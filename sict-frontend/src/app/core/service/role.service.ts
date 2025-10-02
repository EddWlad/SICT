import { Injectable } from '@angular/core';
import { GenericService } from './generic.service';
import { environment } from 'environments/environment.development';
import { Subject } from 'rxjs';
import { Role } from '@core/models/role.model';

@Injectable({
  providedIn: 'root'
})
export class RolesService extends GenericService<Role> {

  protected override url: string = `${environment.HOST}/roles`;
  private rolesChange: Subject<Role[]> = new Subject<Role[]>;
  private messageChange: Subject<string> = new Subject<string>;

  listPageable(pnumber: number, pelement: number) {
    return this.http.get<any>(`${environment.HOST}/roles/pageable?page=${pnumber}&size=${pelement}`);
  }

  setRoleChange(data: Role[]) {
    this.rolesChange.next(data);
  }

  getRoleChange() {
    return this.rolesChange.asObservable();
  }

  setMessageChange(data: string) {
    this.messageChange.next(data);
  }

  getMessageChange() {
    return this.messageChange.asObservable();
  }

}