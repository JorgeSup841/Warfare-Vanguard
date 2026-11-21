import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { usuarioservice } from './usuarioservice';
import { API_BASE_URL } from './api-config';
describe('usuarioservice (Spring Boot real)', () => {
  let auth: usuarioservice;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    auth = TestBed.inject(usuarioservice);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('envía registro al backend directo sin proxy', () => {
    auth
      .registrar({ username: 'jugador', email: 'jugador@ejemplo.com', password: 'A'.repeat(12) })
      .subscribe();
    const peticion = http.expectOne(`${API_BASE_URL}/auth/register`);
    expect(peticion.request.method).toBe('POST');
    peticion.flush({ challengeId: 'reto1', status: 'OTP_SENT' });
  });
  it('envía login y espera el reto OTP', () => {
    auth.iniciarSesion({ username: 'jugador', password: 'A'.repeat(12) }).subscribe();
    const peticion = http.expectOne(`${API_BASE_URL}/auth/login`);
    expect(peticion.request.method).toBe('POST');
    peticion.flush({ challengeId: 'reto2', status: 'OTP_SENT' });
    expect(auth.estaAutenticado()).toBe(false);
  });
  it('el usuario introduce solo el código; challengeId viaja internamente', () => {
    auth.prepararDesafio('login', { challengeId: 'reto2' });
    auth.verificarLogin('123456').subscribe();
    const peticion = http.expectOne(`${API_BASE_URL}/auth/verify-otp`);
    expect(peticion.request.body).toEqual({ challengeId: 'reto2', code: '123456' });
    peticion.flush({
      accessToken: 'header.payload.signature',
      tokenType: 'Bearer',
      user: { id: 1, username: 'jugador', email: 'jugador@ejemplo.com', role: 'USER' },
    });
    expect(auth.getRole()).toBe('USER');
  });
  it('verifica correo con respuesta JSON, no texto sin parsear', () => {
    auth.prepararDesafio('correo', { challengeId: 'reto1' });
    auth
      .verificarCorreo('654321')
      .subscribe((respuesta) => expect(respuesta.message).toBe('Correo verificado'));
    const peticion = http.expectOne(`${API_BASE_URL}/auth/verify-email`);
    expect(peticion.request.body).toEqual({ challengeId: 'reto1', code: '654321' });
    peticion.flush({ message: 'Correo verificado' });
  });
  it('no inicia sesión antes de completar OTP', () => {
    expect(auth.estaAutenticado()).toBe(false);
  });
});
