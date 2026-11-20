
import Phaser from 'phaser';
import { mapa, casilla, terreno, tipoterreno } from '../models/mapa';

export const TAM_CASILLA = 40;

const COLORES_JUGADOR = [
  0xd63031,
  0x0984e3,
  0x00b894,
  0xfdcb6e
];

export interface DatosEscena {
  mapa: mapa;
  terrenos: terreno[];
  bases: { x: number; y: number }[];
  alPasarSobre: (c: casilla | null) => void;
}

/** Escena Phaser que dibuja el terreno, las bases y la interacción con el cursor. */
export class EscenaMapa extends Phaser.Scene {

  private datos!: DatosEscena;
  private resaltado!: Phaser.GameObjects.Graphics;
  private ultimaCasilla: string | null = null;

  constructor() {
    super('EscenaMapa');
  }

  /** Inicializa el mapa y restablece la selección anterior. */
  init(datos: DatosEscena): void {
    this.datos = datos;
    this.ultimaCasilla = null;
  }

  /** Crea las capas de terreno, bases y resaltado para la escena Phaser. */
  create(): void {
    this.dibujarTerreno();
    this.dibujarBases();

    this.resaltado = this.add.graphics();
    this.resaltado.setDepth(10);

    this.activarMouse();
  }

  /** Renderiza cada casilla del mapa según el tipo de terreno. */
  private dibujarTerreno(): void {

    const graficos = this.add.graphics();

    for (const casillaActual of this.datos.mapa.tiles) {

      const px = casillaActual.x * TAM_CASILLA;
      const py = casillaActual.y * TAM_CASILLA;

      graficos.fillStyle(
        this.colorDe(casillaActual.terrainType),
        1
      );

      graficos.fillRect(
        px,
        py,
        TAM_CASILLA,
        TAM_CASILLA
      );

      graficos.lineStyle(1, 0x000000, 0.25);

      graficos.strokeRect(
        px,
        py,
        TAM_CASILLA,
        TAM_CASILLA
      );

      EscenaMapa.decorar(
        graficos,
        casillaActual.terrainType,
        px,
        py
      );
    }
  }

  /** Convierte el color configurado para un terreno en un valor numérico de Phaser. */
  private colorDe(tipo: tipoterreno): number {

    const terrenoEncontrado = this.datos.terrenos.find(
      item => item.terrainType === tipo
    );

    return terrenoEncontrado
      ? Phaser.Display.Color.HexStringToColor(
          terrenoEncontrado.color
        ).color
      : 0xffffff;
  }

  /** Añade los elementos visuales propios de cada tipo de terreno. */
  private static decorar(
    graficos: Phaser.GameObjects.Graphics,
    tipo: tipoterreno,
    px: number,
    py: number
  ): void {

    if (tipo === 'FOREST') {

      graficos.fillStyle(0x2b6b28, 1);

      graficos.fillCircle(
        px + 13,
        py + 24,
        9
      );

      graficos.fillCircle(
        px + 27,
        py + 18,
        9
      );

      graficos.fillCircle(
        px + 22,
        py + 29,
        8
      );

    } else if (tipo === 'MOUNTAIN') {

      graficos.fillStyle(0x6e4f38, 1);

      graficos.fillTriangle(
        px + 4,
        py + 35,
        px + 20,
        py + 6,
        px + 36,
        py + 35
      );

      graficos.fillStyle(0xffffff, 1);

      graficos.fillTriangle(
        px + 14,
        py + 18,
        px + 20,
        py + 6,
        px + 26,
        py + 18
      );

    } else if (tipo === 'WATER') {

      graficos.lineStyle(2, 0xffffff, 0.35);

      graficos.lineBetween(
        px + 6,
        py + 14,
        px + 20,
        py + 14
      );

      graficos.lineBetween(
        px + 16,
        py + 27,
        px + 34,
        py + 27
      );
    }
  }

  /** Dibuja las bases de los jugadores en el tablero. */
  private dibujarBases(): void {

    const graficos = this.add.graphics();

    this.datos.bases.forEach((base, indice) => {

      const px = base.x * TAM_CASILLA;
      const py = base.y * TAM_CASILLA;

      const color =
        COLORES_JUGADOR[indice % COLORES_JUGADOR.length];

      graficos.fillStyle(color, 1);

      graficos.fillRect(
        px + 4,
        py + 4,
        TAM_CASILLA - 8,
        TAM_CASILLA - 8
      );

      graficos.lineStyle(3, 0x14201a, 1);

      graficos.strokeRect(
        px + 4,
        py + 4,
        TAM_CASILLA - 8,
        TAM_CASILLA - 8
      );

      this.add.text(
        px + TAM_CASILLA / 2,
        py + TAM_CASILLA / 2,
        String(indice + 1),
        {
          fontFamily: 'Courier New',
          fontSize: '22px',
          fontStyle: 'bold',
          color: '#14201a'
        }
      ).setOrigin(0.5);

    });
  }

  /** Activa los eventos del puntero y comunica la casilla resaltada. */
  private activarMouse(): void {

    this.input.on(
      'pointermove',
      (puntero: Phaser.Input.Pointer) => {

        const cx = Math.floor(
          puntero.worldX / TAM_CASILLA
        );

        const cy = Math.floor(
          puntero.worldY / TAM_CASILLA
        );

        const mapaActual = this.datos.mapa;

        this.resaltado.clear();

        if (
          cx < 0 ||
          cy < 0 ||
          cx >= mapaActual.width ||
          cy >= mapaActual.height
        ) {

          if (this.ultimaCasilla !== null) {
            this.ultimaCasilla = null;
            this.datos.alPasarSobre(null);
          }

          return;
        }

        this.resaltado.lineStyle(
          3,
          0xffd84d,
          1
        );

        this.resaltado.strokeRect(
          cx * TAM_CASILLA,
          cy * TAM_CASILLA,
          TAM_CASILLA,
          TAM_CASILLA
        );

        const claveCasilla = `${cx},${cy}`;

        if (this.ultimaCasilla === claveCasilla) {
          return;
        }

        this.ultimaCasilla = claveCasilla;

        const casillaActual = mapaActual.tiles.find(
          tile =>
            tile.x === cx &&
            tile.y === cy
        ) ?? null;

        this.datos.alPasarSobre(
          casillaActual
        );
      }
    );

    this.input.on('gameout', () => {

      this.resaltado.clear();

      if (this.ultimaCasilla !== null) {
        this.ultimaCasilla = null;
        this.datos.alPasarSobre(null);
      }
    });
  }
}
