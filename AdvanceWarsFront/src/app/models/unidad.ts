
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

export type tipomovimiento =
  | 'FOOT'
  | 'MECH'
  | 'TREAD'
  | 'WHEEL'
  | 'AIR'
  | 'SHIP';

export type categoria =
  | 'LAND'
  | 'AIR'
  | 'NAVAL';

/**
 * Representa el armamento definido en los catálogos
 * de unidades del frontend.
 */
export interface arma {
  id?: number;
  code: string;
  name: string;
  minRange: number;
  maxRange: number;
  usesAmmunition: boolean;
  ammunitionCapacity: number;
  damageByTarget: {
    [tipo: string]: number;
  };
}

/**
 * Define las características de una unidad
 * disponibles en el catálogo del juego.
 */
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

/**
 * Representa una unidad utilizada por
 * la simulación local del frontend.
 */
export interface unidadjuego {
  id: number;
  unitType: tipounidad;
  ownerOrder: number;
  x: number;
  y: number;
  currentHealth: number;
  currentFuel: number;
  ammunition: {
    [codigoArma: string]: number;
  };
  movedThisTurn: boolean;
  attackedThisTurn: boolean;
  capturedThisTurn: boolean;
}

/**
 * Representa una unidad real recibida desde
 * GET /api/games/{id}/units.
 *
 * Sus propiedades coinciden con UnitDTO
 * del backend Spring Boot.
 */
export interface unidadservidor {
  id: number;
  unitType: string;
  ownerOrder: number;
  x: number;
  y: number;
  health: number;
  fuel: number;
  ammunition?: Record<string, number>;
  movedThisTurn: boolean;
  attackedThisTurn: boolean;
  capturedThisTurn: boolean;
}

/**
 * Representa el resultado de un ataque
 * procesado por Spring Boot.
 *
 * Se utiliza con el endpoint
 * POST /api/games/{id}/units/{unitId}/attack.
 */
export interface resultadoataque {
  attackerUnitId: number;
  targetUnitId: number;
  damageDealt: number;
  counterattackDamage: number;
  attackerHealth: number;
  targetHealth: number;
  attackerDestroyed: boolean;
  targetDestroyed: boolean;
}
