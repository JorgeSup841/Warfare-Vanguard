import { TestBed } from '@angular/core/testing';
import { provideRouter, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { adminGuard } from './admin-guard';
import { usuarioservice } from '../services/usuarioservice';

describe('adminGuard', () => {
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
      adminGuard({} as ActivatedRouteSnapshot, { url: '/partidas' } as RouterStateSnapshot),
    );
    expect(resultado).not.toBe(true);
    expect(resultado).toBeInstanceOf(Object);
  });
  it('permite administración exclusivamente a ADMIN', () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: usuarioservice, useValue: { isAdmin: () => true } },
      ],
    });
    const resultado = TestBed.runInInjectionContext(() =>
      adminGuard({} as ActivatedRouteSnapshot, { url: '/admin' } as RouterStateSnapshot),
    );
    expect(resultado).toBe(true);
  });
});
