import Phaser from 'phaser';
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

  /** Dibuja cada casilla del mapa con color y detalles visuales. */
  private dibujarTerreno(): void {
    const graficos = this.add.graphics();

    for (const casillaActual of this.datos.mapa.tiles) {
      const px = casillaActual.x * TAM_CASILLA;
      const py = casillaActual.y * TAM_CASILLA;
      graficos.fillStyle(this.colorDe(casillaActual.terrainType), 1);
      graficos.fillRect(px, py, TAM_CASILLA, TAM_CASILLA);
      graficos.lineStyle(1, 0x000000, 0.25);
      graficos.strokeRect(px, py, TAM_CASILLA, TAM_CASILLA);
      EscenaMapa.decorar(graficos, casillaActual.terrainType, px, py);
    }
  }

  /**
   * Busca en la lista de terrenos el color de un tipo de terreno.
   * @param tipo tipo de terreno
   * @returns color en formato numérico que entiende Phaser
   */
  private colorDe(tipo: tipoterreno): number {
    const terrenoEncontrado = this.datos.terrenos.find((item) => item.terrainType === tipo);
    return terrenoEncontrado
      ? Phaser.Display.Color.HexStringToColor(terrenoEncontrado.color).color
      : 0xffffff;
  }

  /**
   * Dibuja encima de la casilla: árboles, montaña u olas según el terreno.
   * @param g objeto con el que se dibuja
   * @param tipo tipo de terreno
   * @param px posición x en píxeles
   * @param py posición y en píxeles
   */
  private static decorar(
    graficos: Phaser.GameObjects.Graphics,
    tipo: tipoterreno,
    px: number,
    py: number,
  ): void {
    if (tipo === 'FOREST') {
      graficos.fillStyle(0x2b6b28, 1);
      graficos.fillCircle(px + 13, py + 24, 9);
      graficos.fillCircle(px + 27, py + 18, 9);
      graficos.fillCircle(px + 22, py + 29, 8);
    } else if (tipo === 'MOUNTAIN') {
      graficos.fillStyle(0x6e4f38, 1);
      graficos.fillTriangle(px + 4, py + 35, px + 20, py + 6, px + 36, py + 35);
      graficos.fillStyle(0xffffff, 1);
      graficos.fillTriangle(px + 14, py + 18, px + 20, py + 6, px + 26, py + 18);
    } else if (tipo === 'WATER') {
      graficos.lineStyle(2, 0xffffff, 0.35);
      graficos.lineBetween(px + 6, py + 14, px + 20, py + 14);
      graficos.lineBetween(px + 16, py + 27, px + 34, py + 27);
    }
  }

  /** Dibuja la base y el número de cada jugador. */
  private dibujarBases(): void {
    const graficos = this.add.graphics();

    this.datos.bases.forEach((base, indice) => {
      const px = base.x * TAM_CASILLA;
      const py = base.y * TAM_CASILLA;

      graficos.fillStyle(COLORES_JUGADOR[indice], 1);
      graficos.fillRect(px + 4, py + 4, TAM_CASILLA - 8, TAM_CASILLA - 8);
      graficos.lineStyle(3, 0x14201a, 1);
      graficos.strokeRect(px + 4, py + 4, TAM_CASILLA - 8, TAM_CASILLA - 8);

      this.add
        .text(px + TAM_CASILLA / 2, py + TAM_CASILLA / 2, String(indice + 1), {
          fontFamily: 'Courier New',
          fontSize: '22px',
          fontStyle: 'bold',
          color: '#14201a',
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
      const mapaActual = this.datos.mapa;

      this.resaltado.clear();
      if (cx < 0 || cy < 0 || cx >= mapaActual.width || cy >= mapaActual.height) {
        this.datos.alPasarSobre(null);
        return;
      }

      this.resaltado.lineStyle(3, 0xffd84d, 1);
      this.resaltado.strokeRect(cx * TAM_CASILLA, cy * TAM_CASILLA, TAM_CASILLA, TAM_CASILLA);
      this.datos.alPasarSobre(mapaActual.tiles.find((t) => t.x === cx && t.y === cy) ?? null);
    });

    this.input.on('gameout', () => {
      this.resaltado.clear();
      this.datos.alPasarSobre(null);
    });
  }
}
