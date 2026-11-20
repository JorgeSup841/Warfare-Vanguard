import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { usuarioservice } from '../services/usuarioservice';
// ADMIN también puede ver sus propias partidas: el backend valida su propiedad.
export const jugadorGuard: CanActivateFn = () => {
  const auth = inject(usuarioservice);
  const router = inject(Router);
  return auth.estaAutenticado() && (auth.getRole() === 'USER' || auth.getRole() === 'ADMIN')
    ? true
    : router.createUrlTree(['/']);
};
