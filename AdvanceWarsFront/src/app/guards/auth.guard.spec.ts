import { TestBed } from '@angular/core/testing';
import { provideRouter, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';
import { usuarioservice } from '../services/usuarioservice';

describe('authGuard', () => {
  it('devuelve una redirección si no existe sesión autorizada', () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: usuarioservice,
          useValue: { estaAutenticado: () => false, getRole: () => null, isAdmin: () => false },
        },
      ],
    });
    const resultado = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url: '/partidas' } as RouterStateSnapshot),
    );
    expect(resultado).not.toBe(true);
    expect(resultado).toBeInstanceOf(Object);
  });
  it('permite la ruta si la sesión existe', () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: usuarioservice, useValue: { estaAutenticado: () => true } },
      ],
    });
    const resultado = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url: '/panel' } as RouterStateSnapshot),
    );
    expect(resultado).toBe(true);
  });
});
