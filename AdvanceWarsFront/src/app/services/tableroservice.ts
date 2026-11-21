import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { API_BASE_URL } from './api-config';
import { usuarioservice } from './usuarioservice';
import {
  nuevapartida, partidaresumen, tablero, unidadservidor,
  estadojuego, eventojuego, seleccionjugador, registrarasiento, turnoactual, resultadoCaptura, casillaalcanzable
} from '../models/tablero';
import { resultadoataque, unidad } from '../models/unidad';
import { faccion, comandante } from '../models/partida';

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
  private readonly catalogos = `${API_BASE_URL}/catalog`;

  /** Construye las cabeceras autenticadas para el backend. */
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
  /** Pide las casillas alcanzables al backend sin alterar la partida. */
  movimientosDisponibles(id: number, unidadId: number): Observable<casillaalcanzable[]> {
    return this.http.get<casillaalcanzable[]>(
      `${this.api}/${id}/units/${unidadId}/reachable`,
      { headers: this.headers() }
    );
  }

  /** Inicia la partida seleccionada mediante el backend. */
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

  /** Captura una propiedad bajo una infantería, con el turno confirmado. */
  capturar(id: number, unidadId: number, turno: turnoactual): Observable<resultadoCaptura> {
    return this.http.post<resultadoCaptura>(
      `${this.api}/${id}/units/${unidadId}/capture`, turno,
      { headers: this.headers() }
    );
  }

  /** Produce una unidad en una fábrica propia, si el servidor lo permite. */
  producir(id: number, propertyId: number, unitType: string,
    turno: turnoactual): Observable<unidadservidor> {
    return this.http.post<unidadservidor>(
      `${this.api}/${id}/units/produce`, { propertyId, unitType, ...turno },
      { headers: this.headers() }
    );
  }

  /** Finaliza el turno y devuelve el nuevo GameDTO del servidor. */
  finTurno(id: number, turno: turnoactual): Observable<partidaresumen> {
    return this.http.post<partidaresumen>(
      `${this.api}/${id}/end-turn`, turno,
      { headers: this.headers() }
    );
  }

  /** Catálogos persistidos por el backend. */
  getFacciones(): Observable<faccion[]> {
    return this.http.get<faccion[]>(`${this.catalogos}/factions`, { headers: this.headers() });
  }

  /** Consulta los poderes y estadísticas oficiales de los comandantes. */
  getComandantes(): Observable<comandante[]> {
    return this.http.get<comandante[]>(`${this.catalogos}/commanders`, { headers: this.headers() });
  }

  /** Consulta el catálogo de unidades autorizado por Spring Boot. */
  getCatalogoUnidades(): Observable<unidad[]> {
    return this.http.get<unidad[]>(`${this.catalogos}/units`, { headers: this.headers() });
  }

  /** Actualiza la facción y comandante del jugador local; solo durante SETUP. */
  seleccionarJugador(id: number, orden: number, seleccion: seleccionjugador): Observable<partidaresumen> {
    return this.http.put<partidaresumen>(
      `${this.api}/${id}/players/${orden}/selection`, seleccion,
      { headers: this.headers() }
    );
  }

  /** Activa una habilidad después de validar carga y turno en Spring Boot. */
  poder(id: number, turno: turnoactual): Observable<partidaresumen> {
    return this.http.post<partidaresumen>(`${this.api}/${id}/power`, turno,
      { headers: this.headers() });
  }

  /** Historial autorizado de una partida, persistido por Spring Boot. */
  getLogs(id: number): Observable<eventojuego[]> {
    return this.http.get<eventojuego[]>(`${this.api}/${id}/history`,
      { headers: this.headers() });
  }

}
