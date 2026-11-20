
export interface nuevapartida {
  playerNames: string[];
  width: number;
  height: number;
  seed: number;
}


export interface partidaresumen {
  id: number;
  status: string;
  playerCount: number;
  width: number;
  height: number;
  seed: number;
  playerNames: string[];
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
