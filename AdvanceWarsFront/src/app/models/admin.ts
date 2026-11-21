import { usuario, registro, respuestapendiente } from './usuario';

/** Resumen general de administración recibido desde Spring Boot. */
export interface resumenadmin {
  totalUsers: number;
  onlineUsers: number;
  totalGames: number;
  gamesInProgress: number;
  finishedGames: number;
}

export interface jugadoradmin {
  name: string;
  turnOrder: number;
  money: number;
  status: string;
  factionCode?: string | null;
}

export interface partidainternaadmin {
  id: number;
  status: string;
  playerCount: number;
  width: number;
  height: number;
  seed: number;
  currentRound: number;
  currentPlayerOrder: number;
  winnerPlayerOrder: number | null;
  playerNames: string[];
  players: jugadoradmin[];
}

/** GET /api/admin/games: el DTO incluye un GameDTO anidado. */
export interface partidaadmin {
  game: partidainternaadmin;
  creatorId: number | null;
  creatorUsername: string | null;
  createdAt: string;
}

export interface casillaadmin {
  x: number;
  y: number;
  terrain: string;
  propertyType: string | null;
  propertyId: number | null;
  ownerOrder: number | null;
  captureProgress: number;
  capturingPlayerOrder: number | null;
}

export interface mapaadmin {
  gameId: number;
  width: number;
  height: number;
  tiles: casillaadmin[];
}

export interface tropaadmin {
  id: number;
  unitType: string;
  ownerOrder: number;
  x: number;
  y: number;
  health: number;
  fuel: number;
  movedThisTurn: boolean;
  attackedThisTurn: boolean;
  capturedThisTurn: boolean;
}

export interface registrobatallaadmin {
  id: number;
  eventType: string;
  roundNumber: number;
  turnOrder: number;
  description: string;
  createdAt: string;
}

export interface sesionadmin {
  id: string;
  userId: number;
  username: string;
  loginAt: string;
  lastSeen: string;
  expiresAt: string;
  revoked: boolean;
  online: boolean;
  lastAction: string | null;
}

export interface actividadadmin {
  id: number;
  userId: number;
  username: string;
  action: string;
  createdAt: string;
}

export type { usuario, registro, respuestapendiente };
