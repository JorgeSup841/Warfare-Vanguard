import { TestBed } from '@angular/core/testing';
import { RouterModule, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { adminGuard } from './admin-guard';
import { authGuard } from './auth-guard';

describe('guards Aura Wars', () => {
  it('bloquea partida sin sesión', () => {
    TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([])],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const resultado = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url: '/partidas' } as RouterStateSnapshot),
    );
    expect(resultado).not.toBe(true);
  });

  it('no concede ADMIN sin una sesión ADMIN', () => {
    TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([])],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const resultado = TestBed.runInInjectionContext(() =>
      adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );
    expect(resultado).not.toBe(true);
  });
});
