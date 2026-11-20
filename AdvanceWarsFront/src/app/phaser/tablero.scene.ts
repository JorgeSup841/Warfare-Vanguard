import Phaser from 'phaser';

import { casilla, terreno } from '../models/mapa';
import { faccion, partida } from '../models/partida';

/** Escena Phaser para representar partidas completas y gestionar su interacción. */
export class TableroScene extends Phaser.Scene {
  private readonly tamañoCasilla = 40;

  private datos: partida | null = null;
  private terrenos: terreno[] = [];
  private facciones: faccion[] = [];

  private seleccion: casilla | null = null;
  private listaTextos: Phaser.GameObjects.Text[] = [];

  private capaTerreno?: Phaser.GameObjects.Graphics;
  private capaPropiedades?: Phaser.GameObjects.Graphics;
  private capaUnidades?: Phaser.GameObjects.Graphics;
  private capaSeleccion?: Phaser.GameObjects.Graphics;

  private listaPreparada = false;
  private arrastrando = false;
  private huboArrastre = false;
  private anteriorX = 0;
  private anteriorY = 0;

  constructor(private readonly notificarSeleccion: (seleccion: casilla | null) => void) {
    super({ key: 'TableroScene' });
  }

  /** Prepara las capas gráficas y los eventos del tablero. */
  create(): void {
    this.listaPreparada = true;

    this.capaTerreno = this.add.graphics().setDepth(0);
    this.capaPropiedades = this.add.graphics().setDepth(2);
    this.capaUnidades = this.add.graphics().setDepth(4);
    this.capaSeleccion = this.add.graphics().setDepth(10);

    this.cameras.main.setBackgroundColor('#14201a');

    this.input.mouse?.disableContextMenu();

    this.configurarControles();
    this.dibujarTablero();
  }

  /** Actualiza los datos de partida, los terrenos y las facciones representadas. */
  establecerDatos(datos: partida, terrenos: terreno[], facciones: faccion[]): void {
    this.datos = datos;
    this.terrenos = terrenos;
    this.facciones = facciones;

    if (this.listaPreparada) {
      this.dibujarTablero();
    }
  }

  /** Almacena y destaca la casilla seleccionada. */
  marcarSeleccion(seleccion: casilla | null): void {
    this.seleccion = seleccion;

    if (this.listaPreparada) {
      this.dibujarSeleccion();
    }
  }

  /** Dibuja terrenos, propiedades, unidades e indicadores de vida. */
  private dibujarTablero(): void {
    if (!this.datos || !this.capaTerreno || !this.capaPropiedades || !this.capaUnidades) {
      return;
    }

    this.capaTerreno.clear();
    this.capaPropiedades.clear();
    this.capaUnidades.clear();

    this.listaTextos.forEach((texto) => texto.destroy());
    this.listaTextos = [];

    const tamaño = this.tamañoCasilla;

    // Terrenos
    for (const tile of this.datos.map.tiles) {
      const x = tile.x * tamaño;
      const y = tile.y * tamaño;

      const definicion = this.terrenos.find((t) => t.terrainType === tile.terrainType);

      const color = definicion?.color ?? '#9bd36a';

      this.capaTerreno.fillStyle(TableroScene.colorHexadecimal(color), 1);

      this.capaTerreno.fillRect(x, y, tamaño, tamaño);

      this.capaTerreno.lineStyle(1, 0x14201a, 0.35);
      this.capaTerreno.strokeRect(x, y, tamaño, tamaño);

      if (tile.terrainType === 'FOREST') {
        this.capaTerreno.fillStyle(0x1e542d, 0.85);

        this.capaTerreno.fillTriangle(x + 20, y + 5, x + 7, y + 32, x + 33, y + 32);
      }

      if (tile.terrainType === 'MOUNTAIN') {
        this.capaTerreno.fillStyle(0xe1d5bb, 0.85);

        this.capaTerreno.fillTriangle(x + 20, y + 6, x + 5, y + 34, x + 35, y + 34);
      }

      if (tile.terrainType === 'WATER') {
        this.capaTerreno.lineStyle(2, 0xc8ebff, 0.65);

        this.capaTerreno.lineBetween(x + 6, y + 17, x + 34, y + 17);
      }
    }

    // Propiedades
    for (const propiedad of this.datos.properties) {
      const x = propiedad.x * tamaño;
      const y = propiedad.y * tamaño;

      const color = this.colorJugador(propiedad.ownerOrder);

      this.capaPropiedades.fillStyle(0x203629, 1);
      this.capaPropiedades.fillRect(x + 7, y + 15, 26, 20);

      this.capaPropiedades.fillStyle(color, 1);
      this.capaPropiedades.fillTriangle(x + 4, y + 16, x + 20, y + 4, x + 36, y + 16);

      const letra =
        propiedad.type === 'HEADQUARTERS'
          ? 'HQ'
          : propiedad.type === 'CITY'
            ? 'C'
            : propiedad.type.substring(0, 1);

      this.agregarTexto(letra, x + 10, y + 19, '#ffffff');
    }

    // Unidades
    for (const unidad of this.datos.units) {
      const x = unidad.x * tamaño;
      const y = unidad.y * tamaño;

      const color = this.colorJugador(unidad.ownerOrder);

      this.capaUnidades.fillStyle(0x17291f, 0.5);
      this.capaUnidades.fillEllipse(x + 20, y + 33, 30, 10);

      this.capaUnidades.fillStyle(color, 1);
      this.capaUnidades.fillRoundedRect(x + 6, y + 8, 28, 24, 5);

      this.capaUnidades.lineStyle(2, 0xffffff, 0.8);
      this.capaUnidades.strokeRoundedRect(x + 6, y + 8, 28, 24, 5);

      this.agregarTexto(unidad.unitType.substring(0, 1), x + 15, y + 10, '#15241b');

      // Indicador gráfico de salud de demostración.
      const vida = Math.max(0, Math.min(unidad.currentHealth / 100, 1));

      this.capaUnidades.fillStyle(0x16251b, 1);
      this.capaUnidades.fillRect(x + 5, y + 36, 30, 3);

      this.capaUnidades.fillStyle(0x8eff75, 1);
      this.capaUnidades.fillRect(x + 5, y + 36, 30 * vida, 3);
    }

    const camara = this.cameras.main;

    camara.setBounds(0, 0, this.datos.map.width * tamaño, this.datos.map.height * tamaño);

    this.dibujarSeleccion();
  }

  /** Crea una etiqueta de texto integrada en la escena. */
  private agregarTexto(texto: string, x: number, y: number, color: string): void {
    const objeto = this.add.text(x, y, texto, {
      fontFamily: 'monospace',
      fontSize: '15px',
      fontStyle: 'bold',
      color,
    });

    objeto.setDepth(5);
    this.listaTextos.push(objeto);
  }

  /** Resalta gráficamente la casilla seleccionada. */
  private dibujarSeleccion(): void {
    if (!this.capaSeleccion) {
      return;
    }

    this.capaSeleccion.clear();

    if (!this.seleccion) {
      return;
    }

    const x = this.seleccion.x * this.tamañoCasilla;
    const y = this.seleccion.y * this.tamañoCasilla;

    this.capaSeleccion.fillStyle(0xffffff, 0.18);
    this.capaSeleccion.fillRect(x, y, this.tamañoCasilla, this.tamañoCasilla);

    this.capaSeleccion.lineStyle(3, 0xffd84d, 1);
    this.capaSeleccion.strokeRect(x + 2, y + 2, this.tamañoCasilla - 4, this.tamañoCasilla - 4);
  }

  /** Obtiene el color de la facción propietaria de una casilla o unidad. */
  private colorJugador(orden: number | null): number {
    if (orden === null || !this.datos) {
      return 0xe2dfd2;
    }

    const jugador = this.datos.players.find((j) => j.turnOrder === orden);

    const faccion = this.facciones.find((f) => f.code === jugador?.factionCode);

    return TableroScene.colorHexadecimal(faccion?.color ?? '#d63031');
  }

  /** Convierte un color hexadecimal CSS a un número compatible con Phaser. */
  private static colorHexadecimal(color: string): number {
    return parseInt(color.replace('#', ''), 16);
  }

  /** Configura las acciones del ratón para selección y desplazamiento. */
  private configurarControles(): void {
    this.input.on('pointerdown', (pointer: Phaser.Input.Pointer) => {
      if (pointer.button === 2) {
        this.notificarSeleccion(null);
        return;
      }

      this.arrastrando = true;
      this.huboArrastre = false;

      this.anteriorX = pointer.x;
      this.anteriorY = pointer.y;
    });

    this.input.on('pointermove', (pointer: Phaser.Input.Pointer) => {
      if (!this.arrastrando || !pointer.isDown) {
        return;
      }

      const dx = pointer.x - this.anteriorX;
      const dy = pointer.y - this.anteriorY;

      if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
        this.huboArrastre = true;
      }

      if (this.huboArrastre) {
        const camara = this.cameras.main;

        camara.scrollX -= dx / camara.zoom;
        camara.scrollY -= dy / camara.zoom;
      }

      this.anteriorX = pointer.x;
      this.anteriorY = pointer.y;
    });

    this.input.on('pointerup', (pointer: Phaser.Input.Pointer) => {
      const fueArrastre = this.huboArrastre;

      this.arrastrando = false;
      this.huboArrastre = false;

      if (pointer.button !== 0 || fueArrastre || !this.datos) {
        return;
      }

      const punto = this.cameras.main.getWorldPoint(pointer.x, pointer.y);

      const x = Math.floor(punto.x / this.tamañoCasilla);
      const y = Math.floor(punto.y / this.tamañoCasilla);

      const casillaEncontrada = this.datos.map.tiles.find((tile) => tile.x === x && tile.y === y);

      if (casillaEncontrada) {
        this.notificarSeleccion(casillaEncontrada);
      }
    });

    this.input.on('pointerupoutside', () => {
      this.arrastrando = false;
      this.huboArrastre = false;
    });

    this.input.on(
      'wheel',
      (
        _pointer: Phaser.Input.Pointer,
        _objetos: Phaser.GameObjects.GameObject[],
        _deltaX: number,
        deltaY: number,
      ) => {
        const camara = this.cameras.main;

        const cambio = deltaY > 0 ? -0.1 : 0.1;

        const zoom = Phaser.Math.Clamp(camara.zoom + cambio, 0.6, 2);

        camara.setZoom(zoom);
      },
    );

    this.input.keyboard?.on('keydown-ESC', () => {
      this.notificarSeleccion(null);
    });
  }
}
