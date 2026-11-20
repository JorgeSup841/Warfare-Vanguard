
import Phaser from 'phaser';

import {
  mapa,
  casilla,
  terreno,
  tipoterreno
} from '../models/mapa';

import { unidadservidor } from '../models/unidad';

export const TAM_CASILLA = 40;

const COLORES_JUGADOR = [
  0xd63031,
  0x0984e3,
  0x00b894,
  0xfdcb6e
];

/**
 * Define los datos enviados desde Angular a Phaser.
 */
export interface DatosEscena {
  mapa: mapa;
  terrenos: terreno[];
  bases: { x: number; y: number }[];
  unidades?: unidadservidor[];
  unidadSeleccionadaId?: number | null;
  alPasarSobre: (c: casilla | null) => void;
  alClicCasilla?: (x: number, y: number) => void;
}

/**
 * Escena del tablero táctico de AURA WARS.
 *
 * Dibuja terrenos, bases, unidades y selección.
 * Los datos y las reglas provienen de Angular y Spring Boot.
 */
export class EscenaMapa extends Phaser.Scene {

  private datos!: DatosEscena;

  private resaltado!: Phaser.GameObjects.Graphics;

  private capaUnidades: Phaser.GameObjects.Container | null = null;

  private capaSeleccion: Phaser.GameObjects.Graphics | null = null;

  private ultimaCasilla: string | null = null;

  private unidadMarcadaId: number | null = null;

  constructor() {
    super('EscenaMapa');
  }

  /**
   * Inicializa los datos enviados por Angular.
   *
   * @param datos Información de la escena.
   */
  init(datos: DatosEscena): void {

    this.datos = datos;

    this.ultimaCasilla = null;

    this.unidadMarcadaId =
      datos.unidadSeleccionadaId ?? null;

    this.capaUnidades = null;
    this.capaSeleccion = null;
  }

  /**
   * Crea las capas visuales del tablero.
   */
  create(): void {

    this.dibujarTerreno();

    this.dibujarBases();

    this.dibujarUnidades();

    this.capaSeleccion = this.add.graphics();

    this.capaSeleccion.setDepth(8);

    this.actualizarMarcado();

    this.resaltado = this.add.graphics();

    this.resaltado.setDepth(10);

    this.activarMouse();
  }

  /**
   * Dibuja las casillas y sus decoraciones.
   */
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

  /**
   * Obtiene el color de un tipo de terreno.
   *
   * @param tipo Tipo de terreno.
   * @returns Color numérico de Phaser.
   */
  private colorDe(tipo: tipoterreno): number {

    const encontrado = this.datos.terrenos.find(
      terrenoActual =>
        terrenoActual.terrainType === tipo
    );

    return encontrado
      ? Phaser.Display.Color.HexStringToColor(
        encontrado.color
      ).color
      : 0xffffff;
  }

  /**
   * Añade decoraciones al terreno.
   *
   * @param graficos Objeto Graphics.
   * @param tipo Tipo de terreno.
   * @param px Posición horizontal.
   * @param py Posición vertical.
   */
  private static decorar(
    graficos: Phaser.GameObjects.Graphics,
    tipo: tipoterreno,
    px: number,
    py: number
  ): void {

    if (tipo === 'FOREST') {

      graficos.fillStyle(0x2b6b28, 1);

      graficos.fillCircle(px + 13, py + 24, 9);

      graficos.fillCircle(px + 27, py + 18, 9);

      graficos.fillCircle(px + 22, py + 29, 8);

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

  /**
   * Dibuja las bases de los jugadores.
   */
  private dibujarBases(): void {

    const graficos = this.add.graphics();

    this.datos.bases.forEach((base, indice) => {

      const px = base.x * TAM_CASILLA;
      const py = base.y * TAM_CASILLA;

      const color =
        COLORES_JUGADOR[
        indice % COLORES_JUGADOR.length
          ];

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

  /**
   * Dibuja las unidades recibidas desde Spring Boot.
   *
   * Cada unidad muestra su color, tipo y salud.
   */
  private dibujarUnidades(): void {

    if (this.capaUnidades) {
      this.capaUnidades.destroy(true);
    }

    this.capaUnidades = this.add.container(0, 0);

    this.capaUnidades.setDepth(5);

    const unidadesActuales = this.datos.unidades ?? [];

    for (const unidad of unidadesActuales) {

      if (
        !Number.isInteger(unidad.x) ||
        !Number.isInteger(unidad.y) ||
        unidad.x < 0 ||
        unidad.y < 0 ||
        unidad.x >= this.datos.mapa.width ||
        unidad.y >= this.datos.mapa.height ||
        unidad.health <= 0
      ) {
        continue;
      }

      const px = unidad.x * TAM_CASILLA;
      const py = unidad.y * TAM_CASILLA;

      const color =
        COLORES_JUGADOR[unidad.ownerOrder - 1] ??
        0x95a5a6;

      const rectangulo = this.add.rectangle(
        px + 20,
        py + 20,
        30,
        30,
        color
      );

      rectangulo.setStrokeStyle(
        2,
        0x14201a
      );

      const etiquetaTipo = this.add.text(
        px + 20,
        py + 16,
        unidad.unitType.slice(0, 3).toUpperCase(),
        {
          fontFamily: 'Courier New',
          fontSize: '10px',
          fontStyle: 'bold',
          color: '#ffffff',
          stroke: '#14201a',
          strokeThickness: 3
        }
      );

      etiquetaTipo.setOrigin(0.5);

      const fondoVida = this.add.rectangle(
        px + 20,
        py + 31,
        28,
        12,
        0x14201a,
        0.95
      );

      const etiquetaVida = this.add.text(
        px + 20,
        py + 31,
        String(unidad.health),
        {
          fontFamily: 'Courier New',
          fontSize: '10px',
          fontStyle: 'bold',
          color: '#ffffff'
        }
      );

      etiquetaVida.setOrigin(0.5);

      this.capaUnidades.add([
        rectangulo,
        etiquetaTipo,
        fondoVida,
        etiquetaVida
      ]);
    }
  }

  /**
   * Actualiza la representación de las unidades.
   *
   * Mantiene el mapa y las bases existentes.
   *
   * @param unidades Unidades actualizadas del backend.
   */
  actualizarUnidades(
    unidades: unidadservidor[]
  ): void {

    this.datos.unidades = [...unidades];

    if (
      !this.sys.isActive() ||
      !this.capaUnidades
    ) {
      return;
    }

    this.dibujarUnidades();

    this.actualizarMarcado();
  }

  /**
   * Marca o desmarca una unidad del tablero.
   *
   * Angular determina previamente si la unidad
   * pertenece al jugador actual y puede seleccionarse.
   *
   * @param unidadId ID de la unidad o null para limpiar.
   */
  marcarUnidadSeleccionada(
    unidadId: number | null
  ): void {

    this.unidadMarcadaId = unidadId;

    this.datos.unidadSeleccionadaId = unidadId;

    this.actualizarMarcado();
  }

  /**
   * Dibuja el marco amarillo de la unidad seleccionada.
   *
   * El resaltado permanece al mover el cursor
   * y se actualiza cuando cambian las unidades.
   */
  private actualizarMarcado(): void {

    if (!this.capaSeleccion) {
      return;
    }

    this.capaSeleccion.clear();

    if (this.unidadMarcadaId === null) {
      return;
    }

    const unidad = (this.datos.unidades ?? []).find(
      unidadActual =>
        unidadActual.id === this.unidadMarcadaId &&
        unidadActual.health > 0
    );

    if (!unidad) {
      return;
    }

    const px = unidad.x * TAM_CASILLA;
    const py = unidad.y * TAM_CASILLA;

    this.capaSeleccion.fillStyle(
      0xffd84d,
      0.12
    );

    this.capaSeleccion.fillRect(
      px + 2,
      py + 2,
      TAM_CASILLA - 4,
      TAM_CASILLA - 4
    );

    this.capaSeleccion.lineStyle(
      4,
      0xffd84d,
      1
    );

    this.capaSeleccion.strokeRect(
      px + 2,
      py + 2,
      TAM_CASILLA - 4,
      TAM_CASILLA - 4
    );
  }

  /**
   * Registra el movimiento del cursor y los clics.
   *
   * Envía las coordenadas a Angular sin ejecutar
   * movimientos ni ataques.
   */
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
          0xffffff,
          0.8
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

        this.datos.alPasarSobre(casillaActual);
      }
    );

    this.input.on(
      'pointerdown',
      (puntero: Phaser.Input.Pointer) => {

        if (
          puntero.button !== 0 ||
          !this.datos.alClicCasilla
        ) {
          return;
        }

        const cx = Math.floor(
          puntero.worldX / TAM_CASILLA
        );

        const cy = Math.floor(
          puntero.worldY / TAM_CASILLA
        );

        if (
          cx < 0 ||
          cy < 0 ||
          cx >= this.datos.mapa.width ||
          cy >= this.datos.mapa.height
        ) {
          return;
        }

        this.datos.alClicCasilla(cx, cy);
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
