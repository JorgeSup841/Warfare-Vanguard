import { Injectable } from '@angular/core';
import { configuracionmapa, casilla, tipoterreno } from '../models/mapa';
import { faccion, partida, jugador, propiedadjuego } from '../models/partida';
import { unidad, unidadjuego, tipounidad } from '../models/unidad';

@Injectable({ providedIn: 'root' })
export class demopartidaservice {
  crearDemo(
    cantidad: 2 | 3 | 4,
    config: configuracionmapa,
    facciones: faccion[],
    catalogo: unidad[],
  ): partida {
    const ancho = config.width,
      alto = config.height;
    if (ancho < 8 || alto < 8) throw new Error('El mapa debe tener al menos 8 casillas por lado.');
    if (facciones.length < cantidad) throw new Error('No hay suficientes facciones de prueba.');
    const tiles: casilla[] = [];
    for (let y = 0; y < alto; y++)
      for (let x = 0; x < ancho; x++) {
        tiles.push({ x, y, terrainType: this.terreno(x, y, ancho, alto) });
      }
    const lugares = [
      { x: 1, y: 1 },
      { x: ancho - 2, y: alto - 2 },
      { x: ancho - 2, y: 1 },
      { x: 1, y: alto - 2 },
    ];
    const players: jugador[] = [],
      units: unidadjuego[] = [],
      properties: propiedadjuego[] = [];
    facciones.slice(0, cantidad).forEach((f, i) => {
      const orden = i + 1,
        pos = lugares[i];
      const dx = pos.x < ancho / 2 ? 1 : -1;
      const dy = pos.y < alto / 2 ? 1 : -1;
      players.push({
        id: orden,
        name: f.name,
        turnOrder: orden,
        money: 5000,
        status: 'ACTIVE',
        factionCode: f.code,
        commanderCode: 'DEMO_' + f.code,
      });
      const puntos = [
        { x: pos.x, y: pos.y },
        { x: pos.x + dx, y: pos.y },
        { x: pos.x + 2 * dx, y: pos.y },
        { x: pos.x, y: pos.y + 2 * dy },
      ];
      puntos.forEach((p) => {
        const tile = tiles.find((t) => t.x === p.x && t.y === p.y);
        if (tile) tile.terrainType = 'PLAIN';
      });
      properties.push({
        id: orden * 100 + 1,
        type: 'HEADQUARTERS',
        x: pos.x,
        y: pos.y,
        ownerOrder: orden,
        captureProgress: 0,
      });
      properties.push({
        id: orden * 100 + 2,
        type: 'CITY',
        x: pos.x,
        y: pos.y + 2 * dy,
        ownerOrder: orden,
        captureProgress: 0,
      });
      units.push(this.nuevaUnidad(orden * 100 + 1, 'INFANTRY', orden, pos.x + dx, pos.y, catalogo));
      units.push(
        this.nuevaUnidad(orden * 100 + 2, 'RECON', orden, pos.x + 2 * dx, pos.y, catalogo),
      );
    });
    return {
      id: 1,
      status: 'IN_PROGRESS',
      currentRound: 1,
      currentPlayerOrder: 1,
      winnerPlayerOrder: null,
      seed: 2026,
      map: { width: ancho, height: alto, tiles },
      players,
      units,
      properties,
    };
  }
  private nuevaUnidad(
    id: number,
    tipo: tipounidad,
    orden: number,
    x: number,
    y: number,
    catalogo: unidad[],
  ): unidadjuego {
    const definicion = catalogo.find((u) => u.unitType === tipo);
    const ammunition: Record<string, number> = {};
    definicion?.weapons?.forEach((arma) => {
      if (arma.usesAmmunition) ammunition[arma.code] = arma.ammunitionCapacity;
    });
    return {
      id,
      unitType: tipo,
      ownerOrder: orden,
      x,
      y,
      currentHealth: definicion?.maxHealth ?? 100,
      currentFuel: definicion?.maxFuel ?? 99,
      ammunition,
      movedThisTurn: false,
      attackedThisTurn: false,
      capturedThisTurn: false,
    };
  }
  private terreno(x: number, y: number, ancho: number, alto: number): tipoterreno {
    if (x === Math.floor(ancho / 2) || y === Math.floor(alto / 2)) return 'ROAD';
    if ((x * 7 + y * 11) % 37 < 4) return 'WATER';
    if ((x * 3 + y * 5) % 19 < 3) return 'FOREST';
    if ((x * 13 + y * 17) % 41 < 3) return 'MOUNTAIN';
    return 'PLAIN';
  }
}
