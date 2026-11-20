import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { usuarioservice } from '../services/usuarioservice';
export const authGuard: CanActivateFn = (_ruta, estado) => {
  const auth = inject(usuarioservice);
  const router = inject(Router);
  return auth.estaAutenticado()
    ? true
    : router.createUrlTree(['/'], { queryParams: { returnUrl: estado.url } });
};
