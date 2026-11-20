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
    api.listarUsuarios().subscribe((r) => expect(r).toEqual([]));
    const r = http.expectOne(`${API_BASE_URL}/users`);
    expect(r.request.method).toBe('GET');
    r.flush([]);
  });
  it('cambia el estado por PATCH', () => {
    api.cambiarEstado(2, false).subscribe();
    const r = http.expectOne(`${API_BASE_URL}/users/2/enabled`);
    expect(r.request.method).toBe('PATCH');
    expect(r.request.body).toEqual({ enabled: false });
    r.flush({ id: 2, username: 'u', role: 'USER', enabled: false });
  });
});
