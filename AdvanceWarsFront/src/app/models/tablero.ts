
import { partida } from './partida';

export interface nuevapartida {
  playerNames: string[];
  width: number;
  height: number;
  seed: number;
}

export interface jugadorservidor {
  name: string;
  turnOrder: number;
  money: number;
  status: string;
}

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

export interface casillatablero {
  x: number;
  y: number;
  terrain: string;
  propertyType: string | null;
  ownerOrder: number | null;
}

export interface tablero {
  gameId: number;
  width: number;
  height: number;
  tiles: casillatablero[];
}

export interface unidadservidor {
  id: number;
  unitType: string;
  ownerOrder: number;
  x: number;
  y: number;
  health: number;
  fuel: number;
  movedThisTurn: boolean;
  attackedThisTurn: boolean;
}

export type estadojuego = partida;

export interface seleccionjugador {
  factionId: number;
  commanderId: number;
}

export interface registrarasiento {
  name: string;
  turnOrder?: number;
}

export interface eventojuego {
  id?: number;
  eventType?: string;
  description?: string;
  occurredAt?: string;
}
