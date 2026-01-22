import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const authHeader = authService.getBasicAuthHeader();

  if (authHeader) {
    // Füge Basic Authentication Header hinzu
    const cloned = req.clone({
      setHeaders: {
        Authorization: authHeader
      }
    });
    return next(cloned);
  }

  return next(req);
};
