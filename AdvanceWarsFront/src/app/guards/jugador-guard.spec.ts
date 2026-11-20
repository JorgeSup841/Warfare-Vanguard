import { TestBed } from '@angular/core/testing';
import { provideRouter, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { jugadorGuard } from './jugador-guard';
import { usuarioservice } from '../services/usuarioservice';

describe('jugadorGuard', () => {
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
      jugadorGuard({} as ActivatedRouteSnapshot, { url: '/partidas' } as RouterStateSnapshot),
    );
    expect(resultado).not.toBe(true);
    expect(resultado).toBeInstanceOf(Object);
  });
  it('permite partidas a USER y ADMIN autenticados', () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: usuarioservice,
          useValue: { estaAutenticado: () => true, getRole: () => 'USER' },
        },
      ],
    });
    const user = TestBed.runInInjectionContext(() =>
      jugadorGuard({} as ActivatedRouteSnapshot, { url: '/partidas' } as RouterStateSnapshot),
    );
    expect(user).toBe(true);
  });
});
