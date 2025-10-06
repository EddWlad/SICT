import { inject, Injectable } from '@angular/core';
import { GenericService } from './generic.service';
import { environment } from 'environments/environment.development';
import { Subject } from 'rxjs';
import { Role } from '@core/models/role.model';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class RolesService extends GenericService<Role> {
  constructor() {
    super(
      inject(HttpClient),
      `${environment.HOST}/roles`
    );
   }
}
