import type { partidaresumen, unidadservidor } from '../models/tablero';
import type { unidad as unidadcatalogo } from '../models/unidad';

/** Casilla del alcance visual; el backend sigue validando cada ataque real. */
export interface CasillaAlcanceAtaque {
  x: number;
  y: number;
  objetivoAtacable: boolean;
}

/** Calcula las casillas amenazadas según las armas del catálogo recibido. */
export function calcularAlcanceAtaque(
  atacante: unidadservidor | null,
  catalogo: unidadcatalogo[],
  unidades: unidadservidor[],
  partida: partidaresumen | null,
  ancho: number,
  alto: number,
): CasillaAlcanceAtaque[] {
  if (!atacante || !partida || partida.status !== 'IN_PROGRESS'
    || atacante.health <= 0 || atacante.attackedThisTurn || atacante.capturedThisTurn
    || atacante.ownerOrder !== partida.currentPlayerOrder || ancho < 1 || alto < 1) {
    return [];
  }

  const definicion = catalogo.find(u => u.unitType === atacante.unitType);
  if (!definicion) return [];

  // El comandante amarillo aumenta el alcance máximo de ARTILLERY en 1.
  const comandante = partida.players.find(j => j.turnOrder === atacante.ownerOrder)?.commanderCode;
  const bonificacion = atacante.unitType === 'ARTILLERY' && comandante?.replace(/^CMD_/, '') === 'YELLOW' ? 1 : 0;

  const armas = definicion.weapons.filter(arma =>
    Number.isInteger(arma.minRange) && Number.isInteger(arma.maxRange)
    && arma.minRange >= 1 && arma.maxRange >= arma.minRange
    && !(atacante.movedThisTurn && arma.minRange > 1)
    && (!arma.usesAmmunition || (atacante.ammunition?.[arma.code] ?? 0) > 0)
    && Object.values(arma.damageByTarget ?? {}).some(dano => dano > 0)
  );
  if (!armas.length) return [];

  const enemigos = new Map<string, unidadservidor>();
  for (const unidad of unidades) {
    if (unidad.ownerOrder !== atacante.ownerOrder && unidad.health > 0
      && partida.players.find(j => j.turnOrder === unidad.ownerOrder)?.status !== 'ELIMINATED') {
      enemigos.set(`${unidad.x},${unidad.y}`, unidad);
    }
  }

  const casillas = new Map<string, CasillaAlcanceAtaque>();
  for (const arma of armas) {
    const maximo = arma.maxRange + bonificacion;
    for (let y = Math.max(0, atacante.y - maximo); y <= Math.min(alto - 1, atacante.y + maximo); y++) {
      for (let x = Math.max(0, atacante.x - maximo); x <= Math.min(ancho - 1, atacante.x + maximo); x++) {
        const distancia = Math.abs(atacante.x - x) + Math.abs(atacante.y - y);
        if (distancia < arma.minRange || distancia > maximo) continue;
        const clave = `${x},${y}`;
        const enemigo = enemigos.get(clave);
        const atacable = Boolean(enemigo && (arma.damageByTarget?.[enemigo.unitType] ?? 0) > 0);
        const existente = casillas.get(clave);
        if (existente) existente.objetivoAtacable ||= atacable;
        else casillas.set(clave, { x, y, objetivoAtacable: atacable });
      }
    }
  }
  return [...casillas.values()];
}
