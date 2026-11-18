export type tipoterreno = 'PLAIN' | 'FOREST' | 'MOUNTAIN' | 'WATER' | 'ROAD';

export interface terreno {
  terrainType: tipoterreno;
  name: string;
  defenseStars: number;
  movementCost: { [tipoMovimiento: string]: number };
  color: string;
  spriteUrl?: string;
}

export interface casilla {
  x: number;
  y: number;
  terrainType: tipoterreno;
}

export interface mapa {
  id?: number;
  width: number;
  height: number;
  tiles: casilla[];
}

export interface configuracionmapa {
  width: number;
  height: number;
  minWaterPercent: number;
  maxWaterPercent: number;
  minForestPercent: number;
  maxForestPercent: number;
  minMountainPercent: number;
  maxMountainPercent: number;
  neutralCities: number;
}
