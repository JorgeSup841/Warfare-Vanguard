import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { usuarioservice } from '../services/usuarioservice';
/** Restringe el acceso a los usuarios administradores. */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(usuarioservice);
  const router = inject(Router);
  return auth.isAdmin() ? true : router.createUrlTree(['/panel']);
};
