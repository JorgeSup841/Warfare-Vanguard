import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { usuarioservice } from '../services/usuarioservice';

/** Si ya hay sesión, no deja entrar al login y manda al panel. */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(usuarioservice);
  const router = inject(Router);
  return auth.estaAutenticado() ? router.createUrlTree(['/panel']) : true;
};
