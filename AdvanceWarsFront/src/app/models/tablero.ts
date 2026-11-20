
import { partida } from './partida';

export type { unidadservidor } from './unidad';

/**
 * Representa los datos enviados a Spring Boot
 * para crear una nueva partida.
 */
export interface nuevapartida {
  playerNames: string[];
  width: number;
  height: number;
  seed?: number | null;
}

/**
 * Representa un jugador registrado en una partida.
 * Los datos provienen del GameDTO del backend.
 */
export interface jugadorservidor {
  name: string;
  turnOrder: number;
  money: number;
  status: string;
  factionCode?: string | null;
}

/**
 * Representa el resumen de una partida
 * recibido desde Spring Boot.
 */
export interface partidaresumen {
  id: number;
  status: string;
  playerCount: number;
  width: number;
  height: number;
  seed: number;
  currentRound: number;
  currentPlayerOrder: number;
  playerNames: string[];
  players: jugadorservidor[];
}

/**
 * Representa una casilla del mapa
 * generado por el backend.
 */
export interface casillatablero {
  x: number;
  y: number;
  terrain: string;
  propertyType: string | null;
  ownerOrder: number | null;
  propertyId?: number | null;
  captureProgress?: number;
  capturingPlayerOrder?: number | null;
}

/**
 * Representa el tablero completo de una partida.
 * Contiene sus dimensiones y las casillas recibidas.
 */
export interface tablero {
  gameId: number;
  width: number;
  height: number;
  tiles: casillatablero[];
}

/**
 * Mantiene la compatibilidad con el modelo
 * de partida utilizado en simulaciones locales.
 */
export type estadojuego = partida;

/**
 * Representa la selección de facción
 * y comandante de un jugador.
 */
export interface seleccionjugador {
  factionId: number;
  commanderId: number;
}

/**
 * Representa los datos necesarios
 * para registrar un jugador en una partida.
 */
export interface registrarasiento {
  name: string;
  turnOrder?: number;
}

/**
 * Representa un evento del historial
 * de acciones de una partida.
 */
export interface eventojuego {
  id?: number;
  eventType?: string;
  description?: string;
  occurredAt?: string;
}
export interface turnoactual {
  currentRound: number;
  currentPlayerOrder: number;
}

/** Respuesta real de CaptureResultDTO. */
export interface resultadoCaptura {
  unitId: number;
  propertyId: number;
  propertyType: string;
  x: number;
  y: number;
  captureProgress: number;
  requiredProgress: number;
  ownerOrder: number | null;
  capturingPlayerOrder: number | null;
  captured: boolean;
}


/** Posición disponible y costo de movimiento calculados por Spring Boot. */
export interface casillaalcanzable {
  x: number;
  y: number;
  cost: number;
}
