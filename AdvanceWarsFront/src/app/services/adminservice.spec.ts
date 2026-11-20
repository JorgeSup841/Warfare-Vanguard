import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { adminservice } from './adminservice';
import { API_BASE_URL } from './api-config';
describe('adminservice', () => {
  let api: adminservice;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(adminservice);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('consulta usuarios', () => {
    api.listarUsuarios().subscribe((respuesta) => expect(respuesta).toEqual([]));
    const peticion = http.expectOne(`${API_BASE_URL}/users`);
    expect(peticion.request.method).toBe('GET');
    peticion.flush([]);
  });
  it('cambia el estado por PATCH', () => {
    api.cambiarEstado(2, false).subscribe();
    const peticion = http.expectOne(`${API_BASE_URL}/users/2/enabled`);
    expect(peticion.request.method).toBe('PATCH');
    expect(peticion.request.body).toEqual({ enabled: false });
    peticion.flush({ id: 2, username: 'u', role: 'USER', enabled: false });
  });
});
