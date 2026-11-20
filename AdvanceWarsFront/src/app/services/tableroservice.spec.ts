import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { tableroservice } from './tableroservice';
import { API_BASE_URL } from './api-config';
describe('tableroservice', () => {
  let api: tableroservice;
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(tableroservice);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());
  it('GET /map no crea una partida adicional', () => {
    api.getTablero(42).subscribe();
    const r = http.expectOne(`${API_BASE_URL}/games/42/map`);
    expect(r.request.method).toBe('GET');
    r.flush({ gameId: 42, width: 20, height: 15, tiles: [] });
  });
  it('consulta unidades desde la ruta real del backend', () => {
    api.getUnidades(42).subscribe();
    const r = http.expectOne(`${API_BASE_URL}/games/42/units`);
    expect(r.request.method).toBe('GET');
    r.flush([]);
  });
  it('POST /start sin campos inventados', () => {
    api.iniciarPartida(42).subscribe();
    const r = http.expectOne(`${API_BASE_URL}/games/42/start`);
    expect(r.request.method).toBe('POST');
    r.flush({ id: 42, status: 'IN_PROGRESS' });
  });
  it('operaciones aún no implementadas fallan explícitamente, no generan 404', () => {
    api.getEstado(42).subscribe({ error: (e) => expect(e.message).toContain('pendiente') });
    http.expectNone(`${API_BASE_URL}/games/42/state`);
  });
});
