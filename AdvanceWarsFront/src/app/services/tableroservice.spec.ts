import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { tableroservice } from './tableroservice';
import { API_BASE_URL } from './api-config';

describe('tableroservice', () => {

  let service: tableroservice;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(tableroservice);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('debe crear el servicio correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('debe consultar el mapa sin crear otra partida', () => {

    service.getTablero(42).subscribe();

    const peticion = http.expectOne(
      `${API_BASE_URL}/games/42/map`
    );

    expect(peticion.request.method).toBe('GET');

    peticion.flush({
      gameId: 42,
      width: 20,
      height: 15,
      tiles: []
    });
  });

  it('debe consultar las unidades desde el backend', () => {

    service.getUnidades(42).subscribe();

    const peticion = http.expectOne(
      `${API_BASE_URL}/games/42/units`
    );

    expect(peticion.request.method).toBe('GET');

    peticion.flush([]);
  });

  it('debe iniciar una partida mediante POST', () => {

    service.iniciarPartida(42).subscribe();

    const peticion = http.expectOne(
      `${API_BASE_URL}/games/42/start`
    );

    expect(peticion.request.method).toBe('POST');

    expect(peticion.request.body).toEqual({});

    peticion.flush({
      id: 42,
      status: 'IN_PROGRESS'
    });
  });

  it('solicita las casillas de movimiento con GET y no mueve la unidad', () => {
    service.movimientosDisponibles(42, 7).subscribe(casillas => {
      expect(casillas).toEqual([{ x: 3, y: 4, cost: 2 }]);
    });
    const peticion = http.expectOne(`${API_BASE_URL}/games/42/units/7/reachable`);
    expect(peticion.request.method).toBe('GET');
    peticion.flush([{ x: 3, y: 4, cost: 2 }]);
  });

});
