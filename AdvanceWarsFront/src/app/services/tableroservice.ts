
import { Injectable, inject } from '@angular/core';

import {
  HttpClient,
  HttpHeaders
} from '@angular/common/http';

import {
  Observable,
  throwError
} from 'rxjs';

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
  registrarasiento
} from '../models/tablero';

import {
  resultadoataque
} from '../models/unidad';

/**
 * Información del turno utilizada por Spring Boot
 * para validar movimientos y ataques.
 *
 * Se deriva de partidaresumen para conservar
 * compatibilidad con el modelo actual.
 */
type turnoactual = Pick<
  partidaresumen,
  'currentRound' | 'currentPlayerOrder'
>;

/**
 * Servicio principal de partidas de AURA WARS.
 *
 * Se comunica con Spring Boot mediante HTTP.
 * El backend administra el estado, las reglas,
 * los movimientos y los resultados de combate.
 */
@Injectable({
  providedIn: 'root'
})
export class tableroservice {

  private readonly http = inject(HttpClient);

  private readonly auth = inject(usuarioservice);

  private readonly api = `${API_BASE_URL}/games`;

  /**
   * Obtiene las cabeceras JWT del usuario autenticado.
   *
   * @returns Cabeceras HTTP con autenticación.
   */
  private headers(): HttpHeaders {
    return this.auth.getHeaders();
  }

  /**
   * Crea una partida en Spring Boot.
   *
   * @param datos Configuración de la partida.
   * @returns Resumen de la partida creada.
   */
  crearPartida(
    datos: nuevapartida
  ): Observable<partidaresumen> {

    return this.http.post<partidaresumen>(
      this.api,
      datos,
      {
        headers: this.headers()
      }
    );
  }

  /**
   * Obtiene las partidas disponibles para el usuario.
   *
   * @returns Lista de partidas del servidor.
   */
  listarPartidas(): Observable<partidaresumen[]> {

    return this.http.get<partidaresumen[]>(
      this.api,
      {
        headers: this.headers()
      }
    );
  }

  /**
   * Consulta los datos generales de una partida.
   *
   * @param id Identificador de la partida.
   * @returns Información actualizada de Spring Boot.
   */
  getPartida(
    id: number
  ): Observable<partidaresumen> {

    return this.http.get<partidaresumen>(
      `${this.api}/${id}`,
      {
        headers: this.headers()
      }
    );
  }

  /**
   * Consulta el mapa generado por Spring Boot.
   *
   * @param id Identificador de la partida.
   * @returns Mapa con casillas y propiedades.
   */
  getTablero(
    id: number
  ): Observable<tablero> {

    return this.http.get<tablero>(
      `${this.api}/${id}/map`,
      {
        headers: this.headers()
      }
    );
  }

  /**
   * Obtiene las unidades actuales de una partida.
   *
   * @param id Identificador de la partida.
   * @returns Lista de unidades recibidas del backend.
   */
  getUnidades(
    id: number
  ): Observable<unidadservidor[]> {

    return this.http.get<unidadservidor[]>(
      `${this.api}/${id}/units`,
      {
        headers: this.headers()
      }
    );
  }

  /**
   * Solicita el inicio de una partida.
   *
   * @param id Identificador de la partida.
   * @returns Estado actualizado de la partida.
   */
  iniciarPartida(
    id: number
  ): Observable<partidaresumen> {

    return this.http.post<partidaresumen>(
      `${this.api}/${id}/start`,
      {},
      {
        headers: this.headers()
      }
    );
  }

  /**
   * Descarga el informe PDF de una partida.
   *
   * @param id Identificador de la partida.
   * @returns Archivo PDF en formato Blob.
   */
  descargarPDF(
    id: number
  ): Observable<Blob> {

    return this.http.get(
      `${this.api}/${id}/report.pdf`,
      {
        headers: this.headers(),
        responseType: 'blob'
      }
    );
  }

  /**
   * Devuelve un error explícito para funciones
   * que todavía no están implementadas en Spring Boot.
   *
   * @param accion Nombre de la operación.
   * @param parametros Parámetros de la operación.
   * @returns Observable que emite un error.
   */
  private pendiente(
    accion: string,
    parametros: Record<string, unknown>
  ): Observable<never> {

    return throwError(
      () => new Error(
        `${accion}: pendiente de implementar en Spring Boot ` +
        `(${this.api}; parámetros: ` +
        `${Object.keys(parametros).join(', ')}).`
      )
    );
  }

  /**
   * Reserva la consulta del estado completo.
   *
   * @param id Identificador de la partida.
   * @returns Error de funcionalidad pendiente.
   */
  getEstado(
    id: number
  ): Observable<estadojuego> {

    return this.pendiente(
      'Consultar estado de partida',
      { id }
    );
  }

  /**
   * Reserva el registro de jugadores adicionales.
   *
   * @param id Identificador de la partida.
   * @param datos Información del jugador.
   * @returns Error de funcionalidad pendiente.
   */
  agregarJugador(
    id: number,
    datos: registrarasiento
  ): Observable<unknown> {

    return this.pendiente(
      'Agregar jugador',
      { id, datos }
    );
  }

  /**
   * Reserva la selección de comandante y facción.
   *
   * @param id Identificador de la partida.
   * @param jugadorId Identificador del jugador.
   * @param datos Selección del comandante.
   * @returns Error de funcionalidad pendiente.
   */
  seleccionarComandante(
    id: number,
    jugadorId: number,
    datos: seleccionjugador
  ): Observable<unknown> {

    return this.pendiente(
      'Seleccionar comandante',
      {
        id,
        jugadorId,
        datos
      }
    );
  }

  /**
   * Solicita mover una unidad dentro de la partida.
   *
   * Spring Boot valida:
   * - El turno actual.
   * - El propietario de la unidad.
   * - La disponibilidad del movimiento.
   * - Las reglas y restricciones del tablero.
   *
   * @param id Identificador de la partida.
   * @param unidadId Unidad que desea moverse.
   * @param x Coordenada horizontal solicitada.
   * @param y Coordenada vertical solicitada.
   * @param turno Información del turno actual.
   * @returns Unidad actualizada desde Spring Boot.
   */
  mover(
    id: number,
    unidadId: number,
    x: number,
    y: number,
    turno: turnoactual
  ): Observable<unidadservidor> {

    return this.http.post<unidadservidor>(
      `${this.api}/${id}/units/${unidadId}/move`,
      {
        x,
        y,
        currentRound: turno.currentRound,
        currentPlayerOrder: turno.currentPlayerOrder
      },
      {
        headers: this.headers()
      }
    );
  }

  /**
   * Solicita realizar un ataque entre dos unidades.
   *
   * Angular solamente proporciona los identificadores
   * y la información actual del turno.
   *
   * Spring Boot valida:
   * - Que la partida esté en curso.
   * - Que corresponda al jugador actual.
   * - Que la unidad atacante pueda atacar.
   * - Que el objetivo sea válido.
   * - El alcance y las reglas de combate.
   *
   * El resultado del daño y del contraataque
   * se obtiene exclusivamente del backend.
   *
   * @param id Identificador de la partida.
   * @param unidadId Identificador del atacante.
   * @param objetivoId Identificador del objetivo.
   * @param turno Ronda y jugador actuales.
   * @returns Resultado del combate calculado por Spring Boot.
   */
  atacar(
    id: number,
    unidadId: number,
    objetivoId: number,
    turno: turnoactual
  ): Observable<resultadoataque> {

    return this.http.post<resultadoataque>(
      `${this.api}/${id}/units/${unidadId}/attack`,
      {
        targetUnitId: objetivoId,
        currentRound: turno.currentRound,
        currentPlayerOrder: turno.currentPlayerOrder
      },
      {
        headers: this.headers()
      }
    );
  }

  /**
   * Reserva la captura de propiedades.
   *
   * @param id Identificador de la partida.
   * @param unidadId Unidad que intenta capturar.
   * @returns Error de funcionalidad pendiente.
   */
  capturar(
    id: number,
    unidadId: number
  ): Observable<unknown> {

    return this.pendiente(
      'Capturar propiedad',
      {
        id,
        unidadId
      }
    );
  }

  /**
   * Reserva la producción de unidades.
   *
   * @param id Identificador de la partida.
   * @param propertyId Identificador de la propiedad.
   * @param unitDefinitionId Tipo de unidad.
   * @param spawnX Coordenada horizontal.
   * @param spawnY Coordenada vertical.
   * @returns Error de funcionalidad pendiente.
   */
  producir(
    id: number,
    propertyId: number,
    unitDefinitionId: number,
    spawnX: number,
    spawnY: number
  ): Observable<unknown> {

    return this.pendiente(
      'Producir unidad',
      {
        id,
        propertyId,
        unitDefinitionId,
        spawnX,
        spawnY
      }
    );
  }

  /**
   * Reserva la finalización del turno.
   *
   * Este método pertenece a Persona 2.
   * Se conserva la implementación pendiente
   * de la versión de referencia.
   *
   * @param id Identificador de la partida.
   * @returns Error de funcionalidad pendiente.
   */
  finTurno(
    id: number
  ): Observable<unknown> {

    return this.pendiente(
      'Finalizar turno',
      { id }
    );
  }

  /**
   * Reserva la activación del poder de comandante.
   *
   * @param id Identificador de la partida.
   * @returns Error de funcionalidad pendiente.
   */
  poder(
    id: number
  ): Observable<unknown> {

    return this.pendiente(
      'Activar poder de comandante',
      { id }
    );
  }

  /**
   * Reserva la consulta del historial de acciones.
   *
   * @param id Identificador de la partida.
   * @returns Error de funcionalidad pendiente.
   */
  getLogs(
    id: number
  ): Observable<eventojuego[]> {

    return this.pendiente(
      'Consultar historial de acciones',
      { id }
    );
  }
}
