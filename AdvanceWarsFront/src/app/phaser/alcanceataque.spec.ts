import { describe, expect, it } from 'vitest';
import { calcularAlcanceAtaque } from './alcanceataque';
import type { partidaresumen, unidadservidor } from '../models/tablero';
import type { unidad } from '../models/unidad';

const partida: partidaresumen = {
  id: 1, status: 'IN_PROGRESS', playerCount: 2, width: 9, height: 9,
  seed: 1, currentRound: 1, currentPlayerOrder: 1, playerNames: ['A', 'B'],
  players: [
    { name: 'A', turnOrder: 1, money: 0, status: 'ACTIVE', commanderCode: 'CMD_RED' },
    { name: 'B', turnOrder: 2, money: 0, status: 'ACTIVE' },
  ],
};

const artilleria: unidadservidor = {
  id: 1, unitType: 'ARTILLERY', ownerOrder: 1, x: 4, y: 4,
  health: 100, fuel: 40, movedThisTurn: false, attackedThisTurn: false,
  capturedThisTurn: false, ammunition: { CANNON: 9 },
};
const enemigo: unidadservidor = {
  id: 2, unitType: 'TANK', ownerOrder: 2, x: 4, y: 2,
  health: 100, fuel: 60, movedThisTurn: false, attackedThisTurn: false,
  capturedThisTurn: false,
};
const definicion = {
  unitType: 'ARTILLERY', weapons: [{
    code: 'CANNON', name: 'Cañón', minRange: 2, maxRange: 3,
    usesAmmunition: true, ammunitionCapacity: 9, damageByTarget: { TANK: 70 },
  }],
} as unidad;

function casillas(atacante = artilleria, juego = partida, rivales = [enemigo]) {
  return calcularAlcanceAtaque(atacante, [definicion], [atacante, ...rivales], juego, 9, 9);
}

describe('alcance visual de ataque', () => {
  it('marca el anillo de 2 a 3 casillas y distingue al enemigo alcanzable', () => {
    const resultado = casillas();
    expect(resultado.some(c => c.x === 4 && c.y === 2 && c.objetivoAtacable)).toBe(true);
    expect(resultado.some(c => c.x === 4 && c.y === 3)).toBe(false);
    expect(resultado.some(c => c.x === 4 && c.y === 1)).toBe(true);
  });

  it('aplica el alcance extra del comandante amarillo', () => {
    const amarillo = { ...partida, players: [
      { ...partida.players[0], commanderCode: 'CMD_YELLOW' }, partida.players[1],
    ] };
    expect(casillas(artilleria, amarillo).some(c => c.x === 4 && c.y === 0)).toBe(true);
    expect(casillas().some(c => c.x === 4 && c.y === 0)).toBe(false);
  });

  it('oculta el ataque indirecto tras moverse o quedarse sin munición', () => {
    expect(casillas({ ...artilleria, movedThisTurn: true })).toEqual([]);
    expect(casillas({ ...artilleria, ammunition: { CANNON: 0 } })).toEqual([]);
  });

  it('no marca como atacable un enemigo inmune a las armas disponibles', () => {
    const avion = { ...enemigo, unitType: 'FIGHTER' };
    expect(casillas(artilleria, partida, [avion]).find(c => c.x === 4 && c.y === 2)?.objetivoAtacable).toBe(false);
  });

  it('no presenta zonas de ataque cuando el turno o el ataque no están disponibles', () => {
    expect(casillas({ ...artilleria, attackedThisTurn: true })).toEqual([]);
    expect(casillas(artilleria, { ...partida, currentPlayerOrder: 2 })).toEqual([]);
  });
});
