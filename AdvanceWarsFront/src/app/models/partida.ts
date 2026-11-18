import { mapa } from './mapa';
import { unidadjuego } from './unidad';

export type tipopropiedad = 'CITY' | 'FACTORY' | 'AIRPORT' | 'PORT' | 'HEADQUARTERS';

export interface faccion {
  id?: number;
  code: string;
  name: string;
  description: string;
  color: string;
}

export interface comandante {
  id?: number;
  code: string;
  name: string;
  description: string;
  factionCode: string;
  passiveAbilityName: string;
  passiveAbilityDescription: string;
  passiveEffectCode: string;
  powerName: string;
  powerDescription: string;
  powerEffectCode: string;
  powerCost: number;
  imageUrl?: string;
}

export interface propiedad {
  id?: number;
  type: tipopropiedad;
  name: string;
  income: number;
  defenseBonus: number;
  captureThreshold: number;
  productionCategories: string[];
  spriteUrl?: string;
}

export interface propiedadjuego {
  id: number;
  type: tipopropiedad;
  x: number;
  y: number;
  ownerOrder: number | null;
  captureProgress: number;
}

export interface jugador {
  id?: number;
  name: string;
  turnOrder: number;
  money: number;
  status: 'ACTIVE' | 'ELIMINATED' | 'WINNER';
  factionCode: string;
  commanderCode: string;
}

export interface partida {
  id?: number;
  status: 'SETUP' | 'IN_PROGRESS' | 'FINISHED';
  currentRound: number;
  currentPlayerOrder: number;
  winnerPlayerOrder: number | null;
  seed: number;
  map: mapa;
  players: jugador[];
  units: unidadjuego[];
  properties: propiedadjuego[];
}
