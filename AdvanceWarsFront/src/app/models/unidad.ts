export type tipounidad =
  | 'INFANTRY'
  | 'MECH'
  | 'RECON'
  | 'TANK'
  | 'MEDIUM_TANK'
  | 'ARTILLERY'
  | 'ANTI_AIR'
  | 'ROCKET'
  | 'MISSILE'
  | 'FIGHTER'
  | 'BOMBER'
  | 'BATTLE_COPTER'
  | 'TRANSPORT_COPTER'
  | 'BATTLESHIP'
  | 'CRUISER'
  | 'SUBMARINE';

export type tipomovimiento = 'FOOT' | 'MECH' | 'TREAD' | 'WHEEL' | 'AIR' | 'SHIP';

export type categoria = 'LAND' | 'AIR' | 'NAVAL';

export interface arma {
  id?: number;
  code: string;
  name: string;
  minRange: number;
  maxRange: number;
  usesAmmunition: boolean;
  ammunitionCapacity: number;
  damageByTarget: { [tipo: string]: number };
}

export interface unidad {
  id?: number;
  unitType: tipounidad;
  name: string;
  category: categoria;
  movementType: tipomovimiento;
  productionCost: number;
  maxHealth: number;
  movementPoints: number;
  usesFuel: boolean;
  maxFuel: number;
  fuelConsumptionPerTurn: number;
  fuelPerMovementPoint: number;
  captureCapable: boolean;
  weapons: arma[];
  spriteUrl?: string;
}

export interface unidadjuego {
  id: number;
  unitType: tipounidad;
  ownerOrder: number;
  x: number;
  y: number;
  currentHealth: number;
  currentFuel: number;
  ammunition: { [codigoArma: string]: number };
  movedThisTurn: boolean;
  attackedThisTurn: boolean;
  capturedThisTurn: boolean;
}
