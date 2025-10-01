import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from 'app/app.component';
import { appConfig } from 'app/app.config';

import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { JwtInterceptor } from '../src/app/core/interceptor/jwt.interceptor';
import { ErrorInterceptor } from '../src/app/core/interceptor/error.interceptor';

bootstrapApplication(AppComponent, {
  // conservamos todo lo que ya tengas en appConfig
  ...appConfig,
  // y añadimos nuestros providers sin perder los existentes
  providers: [
    ...(appConfig.providers ?? []),
    // necesario para que Angular use los interceptores DI en standalone
    provideHttpClient(withInterceptorsFromDi()),
    // registra tus dos interceptores (en este orden)
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor,   multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
  ],
}).catch((err) => console.error(err));
