import { Route } from '@angular/router';
import { MainLayoutComponent } from './layout/app-layout/main-layout/main-layout.component';
import { AuthGuard } from '@core/guard/auth.guard';
import { AuthLayoutComponent } from './layout/app-layout/auth-layout/auth-layout.component';
import { Page404Component } from './authentication/page404/page404.component';

export const APP_ROUTE: Route[] = [
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: '/authentication/signin', pathMatch: 'full' },
      {
        path: 'advance-table',
        loadChildren: () =>
          import('./advance-table/advance-table.routes').then(
            (m) => m.ADVANCE_TABLE_ROUTE
          ),
      },
      {
        path: 'role',
        loadChildren: () =>
          import('./role/role.routes').then(
            (m) => m.ROLE_ROUTE
          ),
      },
      {
        path: 'user',
        loadChildren: () =>
          import('./user/user.routes').then(
            (m) => m.USER_ROUTE
          ),
      },
    ],
  },
  {
    path: 'authentication',
    component: AuthLayoutComponent,
    loadChildren: () =>
      import('./authentication/auth.routes').then((m) => m.AUTH_ROUTE),
  },
  { path: '**', component: Page404Component },
];
