import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

import { API_BASE_URL } from './api-config';
import { usuarioservice } from './usuarioservice';

import {
  nuevapartida,
  partidaresumen,
  tablero,
  unidadservidor,
  estadojuego,
  eventojuego,
  seleccionjugador,
  registrarasiento,
  turnoactual,
} from '../models/tablero';

/** Accede a los endpoints disponibles de partidas y señala los que todavía no están implementados. */
@Injectable({
  providedIn: 'root',
})
export class tableroservice {
  private http = inject(HttpClient);
  private auth = inject(usuarioservice);
  private readonly api = `${API_BASE_URL}/games`;
  /** Construye las cabeceras HTTP de la sesión JWT. */
  private headers(): HttpHeaders {
    return this.auth.getHeaders();
  }

  /** Crea una partida mediante el endpoint de juegos. */
  crearPartida(datos: nuevapartida): Observable<partidaresumen> {
    return this.http.post<partidaresumen>(this.api, datos, { headers: this.headers() });
  }

  /** Consulta el listado de partidas existentes. */
  listarPartidas(): Observable<partidaresumen[]> {
    return this.http.get<partidaresumen[]>(this.api, { headers: this.headers() });
  }

  /** Consulta el resumen de una partida según su identificador. */
  getPartida(id: number): Observable<partidaresumen> {
    return this.http.get<partidaresumen>(`${this.api}/${id}`, { headers: this.headers() });
  }

  /** Recupera las casillas del tablero generado por el servidor. */
  getTablero(id: number): Observable<tablero> {
    return this.http.get<tablero>(`${this.api}/${id}/map`, { headers: this.headers() });
  }

  /** Recupera las unidades presentes en una partida. */
  getUnidades(id: number): Observable<unidadservidor[]> {
    return this.http.get<unidadservidor[]>(`${this.api}/${id}/units`, { headers: this.headers() });
  }

  /** Solicita al servidor iniciar una partida en preparación. */
  iniciarPartida(id: number): Observable<partidaresumen> {
    return this.http.post<partidaresumen>(
      `${this.api}/${id}/start`,
      {},
      { headers: this.headers() },
    );
  }

  /** Obtiene el informe PDF de una partida como Blob. */
  descargarPDF(id: number): Observable<Blob> {
    return this.http.get(`${this.api}/${id}/report.pdf`, {
      headers: this.headers(),
      responseType: 'blob',
    });
  }

  /** Informa explícitamente que una operación aún no existe en el backend. */
  private pendiente(accion: string, parametros: Record<string, unknown>): Observable<never> {
    return throwError(
      () =>
        new Error(
          `${accion}: pendiente de implementar en Spring Boot (${this.api}; parámetros: ${Object.keys(parametros).join(', ')}).`,
        ),
    );
  }

  /** Reserva la consulta del estado completo de una partida. */
  getEstado(id: number): Observable<estadojuego> {
    return this.pendiente('Consultar estado de partida', { id });
  }

  /** Reserva el registro de un jugador en la partida. */
  agregarJugador(id: number, datos: registrarasiento): Observable<unknown> {
    return this.pendiente('Agregar jugador', { id, datos });
  }

  /** Reserva la selección de comandante y facción del jugador. */
  seleccionarComandante(
    id: number,
    jugadorId: number,
    datos: seleccionjugador,
  ): Observable<unknown> {
    return this.pendiente('Seleccionar comandante', { id, jugadorId, datos });
  }

  /** Reserva el movimiento de una unidad hacia una casilla. */
  mover(id: number, unidadId: number, x: number, y: number): Observable<unknown> {
    return this.pendiente('Mover unidad', { id, unidadId, x, y });
  }

  /** Reserva una acción de ataque entre unidades. */
  atacar(id: number, unidadId: number, targetUnitId: number): Observable<unknown> {
    return this.pendiente('Atacar unidad', { id, unidadId, targetUnitId });
  }

  /** Reserva la captura de una propiedad por una unidad. */
  capturar(id: number, unidadId: number): Observable<unknown> {
    return this.pendiente('Capturar propiedad', { id, unidadId });
  }

  /** Reserva la producción de unidades en una propiedad. */
  producir(
    id: number,
    propertyId: number,
    unitDefinitionId: number,
    spawnX: number,
    spawnY: number,
  ): Observable<unknown> {
    return this.pendiente('Producir unidad', { id, propertyId, unitDefinitionId, spawnX, spawnY });
  }

  /** Termina el turno del jugador actual y devuelve la partida actualizada. */
  finTurno(id: number, turno: turnoactual): Observable<partidaresumen> {
    return this.http.post<partidaresumen>(`${this.api}/${id}/end-turn`, turno, {
      headers: this.headers(),
    });
  }

  /** Reserva la activación de un poder de comandante. */
  poder(id: number): Observable<unknown> {
    return this.pendiente('Activar poder de comandante', { id });
  }

  /** Reserva la consulta del historial de acciones. */
  getLogs(id: number): Observable<eventojuego[]> {
    return this.pendiente('Consultar historial de acciones', { id });
  }
}
