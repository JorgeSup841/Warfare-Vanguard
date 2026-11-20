import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { API_BASE_URL } from './api-config';
import {
  nuevapartida,
  partidaresumen,
  tablero,
  unidadservidor,
  estadojuego,
  eventojuego,
  seleccionjugador,
  registrarasiento,
} from '../models/tablero';
import { usuarioservice } from './usuarioservice';

@Injectable({ providedIn: 'root' })
export class tableroservice {
  private readonly api = `${API_BASE_URL}/games`;
  constructor(
    private http: HttpClient,
    private auth: usuarioservice,
  ) {}
  private headers(): HttpHeaders {
    return this.auth.getHeaders();
  }
  crearPartida(datos: nuevapartida): Observable<partidaresumen> {
    return this.http.post<partidaresumen>(this.api, datos, { headers: this.headers() });
  }
  listarPartidas(): Observable<partidaresumen[]> {
    return this.http.get<partidaresumen[]>(this.api, { headers: this.headers() });
  }
  getPartida(id: number): Observable<partidaresumen> {
    return this.http.get<partidaresumen>(`${this.api}/${id}`, { headers: this.headers() });
  }
  getTablero(id: number): Observable<tablero> {
    return this.http.get<tablero>(`${this.api}/${id}/map`, { headers: this.headers() });
  }
  getUnidades(id: number): Observable<unidadservidor[]> {
    return this.http.get<unidadservidor[]>(`${this.api}/${id}/units`, { headers: this.headers() });
  }
  iniciarPartida(id: number): Observable<partidaresumen> {
    return this.http.post<partidaresumen>(
      `${this.api}/${id}/start`,
      {},
      { headers: this.headers() },
    );
  }
  descargarPDF(id: number): Observable<Blob> {
    return this.http.get(`${this.api}/${id}/report.pdf`, {
      headers: this.headers(),
      responseType: 'blob',
    });
  }
  // Métodos previstos por el plan maestro: NO están implementados en el backend recibido.
  // Error explícito en lugar de disparar llamadas 404 que parezcan una integración real.
  private pendiente(accion: string): Observable<never> {
    return throwError(() => new Error(`${accion}: pendiente de implementar en Spring Boot.`));
  }
  getEstado(id: number): Observable<estadojuego> {
    return this.pendiente('GET /api/games/:id/state');
  }
  agregarJugador(id: number, datos: registrarasiento): Observable<unknown> {
    return this.pendiente('POST /api/games/:id/players');
  }
  seleccionarComandante(
    id: number,
    jugadorId: number,
    datos: seleccionjugador,
  ): Observable<unknown> {
    return this.pendiente('PUT /api/games/:id/players/:jugadorId/selection');
  }
  mover(id: number, unidadId: number, x: number, y: number): Observable<unknown> {
    return this.pendiente('Mover unidad');
  }
  atacar(id: number, unidadId: number, targetUnitId: number): Observable<unknown> {
    return this.pendiente('Atacar');
  }
  capturar(id: number, unidadId: number): Observable<unknown> {
    return this.pendiente('Capturar propiedad');
  }
  producir(
    id: number,
    propertyId: number,
    unitDefinitionId: number,
    spawnX: number,
    spawnY: number,
  ): Observable<unknown> {
    return this.pendiente('Producir');
  }
  finTurno(id: number): Observable<unknown> {
    return this.pendiente('Finalizar turno');
  }
  poder(id: number): Observable<unknown> {
    return this.pendiente('Poder de comandante');
  }
  getLogs(id: number): Observable<eventojuego[]> {
    return this.pendiente('Historial de acciones');
  }
}
