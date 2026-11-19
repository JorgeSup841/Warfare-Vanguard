import * as Phaser from 'phaser';
import { mapa, casilla, terreno, tipoterreno } from '../models/mapa';

/** Tamaño en píxeles de cada casilla del tablero. */
export const TAM_CASILLA = 40;

/** Color de cada jugador: rojo, azul, verde y amarillo. */
const COLORES_JUGADOR = [0xd63031, 0x0984e3, 0x00b894, 0xfdcb6e];

/**
 * Datos que Angular le entrega a la escena de Phaser.
 */
export interface DatosEscena {
  mapa: mapa;
  terrenos: terreno[];
  bases: { x: number; y: number }[];
  alPasarSobre: (c: casilla | null) => void;
}

/**
 * Escena de Phaser que dibuja el tablero: terreno, bases y la casilla bajo el mouse.
 */
export class EscenaMapa extends Phaser.Scene {
  private datos!: DatosEscena;
  private resaltado!: Phaser.GameObjects.Graphics;

  constructor() {
    super('EscenaMapa');
  }

  /**
   * Phaser llama a este método primero y aquí recibimos los datos de Angular.
   * @param datos mapa, terrenos, bases y la función para avisar a Angular
   */
  init(datos: DatosEscena): void {
    this.datos = datos;
  }

  /**
   * Phaser llama a este método cuando la escena está lista para dibujar.
   */
  create(): void {
    this.dibujarTerreno();
    this.dibujarBases();
    this.resaltado = this.add.graphics();
    this.activarMouse();
  }

  /**
   * Dibuja cada casilla del mapa con el color de su terreno y un pequeño dibujo encima.
   */
  private dibujarTerreno(): void {
    const g = this.add.graphics();

    for (const c of this.datos.mapa.tiles) {
      const px = c.x * TAM_CASILLA;
      const py = c.y * TAM_CASILLA;

      g.fillStyle(this.colorDe(c.terrainType), 1);
      g.fillRect(px, py, TAM_CASILLA, TAM_CASILLA);
      this.decorar(g, c.terrainType, px, py);

      g.lineStyle(1, 0x000000, 0.15);
      g.strokeRect(px, py, TAM_CASILLA, TAM_CASILLA);
    }
  }

  /**
   * Busca en la lista de terrenos el color de un tipo de terreno.
   * @param tipo tipo de terreno
   * @returns color en formato numérico que entiende Phaser
   */
  private colorDe(tipo: tipoterreno): number {
    const t = this.datos.terrenos.find(x => x.terrainType === tipo);
    return t ? Phaser.Display.Color.HexStringToColor(t.color).color : 0xffffff;
  }

  /**
   * Dibuja encima de la casilla: árboles, montaña u olas según el terreno.
   * @param g objeto con el que se dibuja
   * @param tipo tipo de terreno
   * @param px posición x en píxeles
   * @param py posición y en píxeles
   */
  private decorar(g: Phaser.GameObjects.Graphics, tipo: tipoterreno, px: number, py: number): void {
    if (tipo === 'FOREST') {
      g.fillStyle(0x2b6b28, 1);
      g.fillCircle(px + 13, py + 24, 9);
      g.fillCircle(px + 27, py + 18, 9);
      g.fillCircle(px + 22, py + 29, 8);
    } else if (tipo === 'MOUNTAIN') {
      g.fillStyle(0x6e4f38, 1);
      g.fillTriangle(px + 4, py + 35, px + 20, py + 6, px + 36, py + 35);
      g.fillStyle(0xffffff, 1);
      g.fillTriangle(px + 14, py + 18, px + 20, py + 6, px + 26, py + 18);
    } else if (tipo === 'WATER') {
      g.lineStyle(2, 0xffffff, 0.35);
      g.lineBetween(px + 6, py + 14, px + 20, py + 14);
      g.lineBetween(px + 16, py + 27, px + 34, py + 27);
    }
  }

  /**
   * Dibuja un cuadro del color del jugador con su número sobre cada base.
   */
  private dibujarBases(): void {
    const g = this.add.graphics();

    this.datos.bases.forEach((base, i) => {
      const px = base.x * TAM_CASILLA;
      const py = base.y * TAM_CASILLA;

      g.fillStyle(COLORES_JUGADOR[i], 1);
      g.fillRect(px + 4, py + 4, TAM_CASILLA - 8, TAM_CASILLA - 8);
      g.lineStyle(3, 0x14201a, 1);
      g.strokeRect(px + 4, py + 4, TAM_CASILLA - 8, TAM_CASILLA - 8);

      this.add
        .text(px + TAM_CASILLA / 2, py + TAM_CASILLA / 2, String(i + 1), {
          fontFamily: 'Courier New',
          fontSize: '22px',
          fontStyle: 'bold',
          color: '#14201a'
        })
        .setOrigin(0.5);
    });
  }

  /**
   * Marca con un borde amarillo la casilla bajo el mouse y le avisa a Angular cuál es.
   */
  private activarMouse(): void {
    this.input.on('pointermove', (puntero: Phaser.Input.Pointer) => {
      const cx = Math.floor(puntero.worldX / TAM_CASILLA);
      const cy = Math.floor(puntero.worldY / TAM_CASILLA);
      const m = this.datos.mapa;

      this.resaltado.clear();
      if (cx < 0 || cy < 0 || cx >= m.width || cy >= m.height) {
        this.datos.alPasarSobre(null);
        return;
      }

      this.resaltado.lineStyle(3, 0xffd84d, 1);
      this.resaltado.strokeRect(cx * TAM_CASILLA, cy * TAM_CASILLA, TAM_CASILLA, TAM_CASILLA);
      this.datos.alPasarSobre(m.tiles[cy * m.width + cx]);
    });

    this.input.on('gameout', () => {
      this.resaltado.clear();
      this.datos.alPasarSobre(null);
    });
  }
}
