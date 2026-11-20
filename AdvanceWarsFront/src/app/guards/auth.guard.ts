import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { usuarioservice } from '../services/usuarioservice';
/** Restringe la navegación cuando no hay una sesión válida. */
export const authGuard: CanActivateFn = (_ruta, estado) => {
  const auth = inject(usuarioservice);
  const router = inject(Router);
  return auth.estaAutenticado()
    ? true
    : router.createUrlTree(['/'], { queryParams: { returnUrl: estado.url } });
};
