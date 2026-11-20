import Phaser from 'phaser';
import { mapa, casilla, terreno, tipoterreno } from '../models/mapa';
import { casillaalcanzable, casillatablero } from '../models/tablero';
import { resultadoataque, unidadservidor } from '../models/unidad';

export const TAM_CASILLA = 40;

const RAIZ = '/assets/sprites/';
const TERRENOS = ['PLAIN', 'FOREST', 'MOUNTAIN', 'WATER', 'ROAD'] as const;
const PROPIEDADES = ['HEADQUARTERS', 'CITY', 'FACTORY', 'AIRPORT', 'PORT'] as const;
const TIPOS = [
  'INFANTRY', 'MECH', 'RECON', 'TANK', 'MEDIUM_TANK', 'ARTILLERY',
  'ANTI_AIR', 'ROCKET', 'MISSILE', 'FIGHTER', 'BOMBER', 'BATTLE_COPTER',
  'TRANSPORT_COPTER', 'BATTLESHIP', 'CRUISER', 'SUBMARINE'
] as const;
const FACCIONES = ['RED', 'BLUE', 'GREEN', 'YELLOW'] as const;
type AnimacionUnidad = 'idle' | 'move' | 'attack' | 'hit' | 'destroy';
const CUADROS: Record<AnimacionUnidad, number> = {
  idle: 2, move: 4, attack: 4, hit: 2, destroy: 6
};
const COLORES = [0xd63031, 0x0984e3, 0x00b894, 0xfdcb6e];

/** Contrato Phaser ← Angular: los datos y la autoridad siguen en Spring Boot. */
export interface DatosEscena {
  mapa: mapa;
  terrenos: terreno[];
  bases: { x: number; y: number }[];
  propiedades?: casillatablero[];
  faccionesPorOrden?: Record<number, string | null>;
  unidades?: unidadservidor[];
  unidadSeleccionadaId?: number | null;
  movimientosDisponibles?: casillaalcanzable[];
  alPasarSobre: (casilla: casilla | null) => void;
  alClicCasilla?: (x: number, y: number) => void;
}

interface VistaUnidad {
  grupo: Phaser.GameObjects.Container;
  sprite: Phaser.GameObjects.Sprite;
  salud: Phaser.GameObjects.Text;
  tipo: string;
  propietario: number;
  x: number;
  y: number;
}

/** Escena activa del juego: capas separadas y sprites del paquete AURA WARS. */
export class EscenaMapa extends Phaser.Scene {
  private datos!: DatosEscena;
  private capaTerreno!: Phaser.GameObjects.Container;
  private capaEdificios!: Phaser.GameObjects.Container;
  private capaUnidades!: Phaser.GameObjects.Container;
  private capaSeleccion!: Phaser.GameObjects.Graphics;
  private capaMovimientos!: Phaser.GameObjects.Graphics;
  private resaltado!: Phaser.GameObjects.Graphics;
  private readonly vistas = new Map<number, VistaUnidad>();
  private ultimaCasilla: string | null = null;
  private unidadMarcadaId: number | null = null;

  constructor() { super('EscenaMapa'); }

  /** Recibe una instantánea del backend o del modo demostración. */
  init(datos: DatosEscena): void {
    this.datos = datos;
    this.ultimaCasilla = null;
    this.unidadMarcadaId = datos.unidadSeleccionadaId ?? null;
    this.vistas.clear();
  }

  /** Solo precarga 5 terrenos, 20 edificios, 64 unidades y animaciones de tipos presentes. */
  preload(): void {
    if (!this.cache.json.exists('aura-manifiesto')) {
      this.load.json('aura-manifiesto', `${RAIZ}sprites-manifest.json`);
    }
    for (const tipo of TERRENOS) {
      this.cargarImagen(`terreno-${tipo}`, `${RAIZ}terrenos/${tipo}.png`);
    }
    for (const faccion of FACCIONES) {
      for (const propiedad of PROPIEDADES) {
        this.cargarImagen(`edificio-${faccion}-${propiedad}`,
          `${RAIZ}edificios/${faccion}/${propiedad}.png`);
      }
      for (const tipo of TIPOS) {
        this.cargarImagen(`unidad-${faccion}-${tipo}`, `${RAIZ}unidades/${faccion}/${tipo}.png`);
      }
    }
    for (let i = 0; i < 6; i++) {
      const numero = String(i).padStart(2, '0');
      this.cargarImagen(`efecto-explosion-${numero}`,
        `${RAIZ}animaciones/efectos/explosion_${numero}.png`);
    }
    // No se descargan las casi dos mil imágenes animadas por cada inicio.
    const presentes = new Set((this.datos.unidades ?? [])
      .filter(u => this.faccion(u.ownerOrder) !== null && this.tipoConocido(u.unitType))
      .map(u => `${this.faccion(u.ownerOrder)}:${u.unitType}`));
    for (const presente of presentes) {
      const [faccion, tipo] = presente.split(':');
      for (const accion of Object.keys(CUADROS) as AnimacionUnidad[]) {
        for (let i = 0; i < CUADROS[accion]; i++) {
          const numero = String(i).padStart(2, '0');
          const clave = `anim-${faccion}-${tipo}-${accion}-${numero}`;
          this.cargarImagen(clave,
            `${RAIZ}animaciones/unidades/${faccion}/${tipo}/${accion}_${numero}.png`);
        }
      }
    }
  }

  /** Evita volver a registrar texturas al reiniciar una escena Phaser. */
  private cargarImagen(clave: string, url: string): void {
    if (!this.textures.exists(clave)) this.load.image(clave, url);
  }

  /** Crea el tablero conservando una jerarquía fija de profundidad. */
  create(): void {
    this.cameras.main.setBackgroundColor('#14201a');
    this.cameras.main.setBounds(0, 0,
      this.datos.mapa.width * TAM_CASILLA,
      this.datos.mapa.height * TAM_CASILLA);
    this.capaTerreno = this.add.container(0, 0).setDepth(0);
    this.capaEdificios = this.add.container(0, 0).setDepth(2);
    this.capaUnidades = this.add.container(0, 0).setDepth(5);
    this.capaMovimientos = this.add.graphics().setDepth(6);
    this.capaSeleccion = this.add.graphics().setDepth(8);
    this.resaltado = this.add.graphics().setDepth(10);
    this.dibujarTerrenos();
    this.dibujarPropiedades();
    this.actualizarUnidades(this.datos.unidades ?? [], false);
    this.actualizarMarcado();
    this.dibujarMovimientosDisponibles();
    this.activarMouse();
  }

  private faccion(orden: number | null | undefined): string | null {
    if (!Number.isInteger(orden) || orden === null || orden === undefined || orden < 1 || orden > 4) {
      return null;
    }
    const asignada = this.datos.faccionesPorOrden?.[orden];
    if (asignada && (FACCIONES as readonly string[]).includes(asignada)) return asignada;
    // El backend aún puede no tener una facción elegida; convención visual local.
    return FACCIONES[orden - 1];
  }

  private tipoConocido(tipo: string): boolean {
    return (TIPOS as readonly string[]).includes(tipo);
  }

  private claveUnidad(unidad: unidadservidor): string | null {
    const faccion = this.faccion(unidad.ownerOrder);
    if (!faccion || !this.tipoConocido(unidad.unitType)) return null;
    const clave = `unidad-${faccion}-${unidad.unitType}`;
    return this.textures.exists(clave) ? clave : null;
  }

  private dibujarTerrenos(): void {
    const cuadricula = this.add.graphics().setDepth(1);
    for (const actual of this.datos.mapa.tiles) {
      const px = actual.x * TAM_CASILLA;
      const py = actual.y * TAM_CASILLA;
      const clave = `terreno-${actual.terrainType}`;
      if (this.textures.exists(clave)) {
        this.capaTerreno.add(this.add.image(px + 20, py + 20, clave));
      } else {
        const definicion = this.datos.terrenos.find(t => t.terrainType === actual.terrainType);
        const color = definicion?.color ?? '#789c74';
        const recuadro = this.add.rectangle(px + 20, py + 20, 40, 40,
          Phaser.Display.Color.HexStringToColor(color).color);
        this.capaTerreno.add(recuadro);
      }
      cuadricula.lineStyle(1, 0x14201a, 0.30);
      cuadricula.strokeRect(px, py, TAM_CASILLA, TAM_CASILLA);
    }
  }

  private dibujarPropiedades(): void {
    this.capaEdificios.removeAll(true);
    const propiedades = (this.datos.propiedades ?? [])
      .filter(t => t.propertyType !== null);
    // Compatibilidad con la versión anterior del modo demo.
    if (propiedades.length === 0) {
      this.datos.bases.forEach((base, indice) => {
        propiedades.push({ x: base.x, y: base.y, terrain: 'PLAIN',
          propertyType: 'HEADQUARTERS', ownerOrder: indice + 1 });
      });
    }
    for (const propiedad of propiedades) {
      const px = propiedad.x * TAM_CASILLA + 20;
      const py = propiedad.y * TAM_CASILLA + 20;
      const faccion = this.faccion(propiedad.ownerOrder);
      const clave = faccion ? `edificio-${faccion}-${propiedad.propertyType}` : null;
      if (clave && this.textures.exists(clave)) {
        this.capaEdificios.add(this.add.image(px, py, clave));
      } else {
        // Propiedad neutral o código futuro: nunca se atribuye al jugador rojo.
        const base = this.add.rectangle(px, py, 32, 32, 0x879b90, 0.93);
        base.setStrokeStyle(2, 0x354e45);
        const sigla = (propiedad.propertyType ?? '?').slice(0, 2);
        const nombre = this.add.text(px, py, sigla, {
          fontFamily: 'monospace', fontSize: '12px', color: '#f5f2df'
        }).setOrigin(0.5);
        this.capaEdificios.add([base, nombre]);
      }
      if ((propiedad.captureProgress ?? 0) > 0) {
        const avance = this.add.text(px, py + 12, `${propiedad.captureProgress}`, {
          fontFamily: 'monospace', fontSize: '10px', color: '#ffffff',
          backgroundColor: '#14201a'
        }).setOrigin(0.5);
        this.capaEdificios.add(avance);
      }
    }
  }

  /** Actualiza únicamente unidades nuevas, cambiadas o destruidas. */
  actualizarUnidades(unidades: unidadservidor[], animar = true): void {
    this.datos.unidades = [...unidades];
    if (!this.capaUnidades) return;
    const vivas = new Set<number>();
    for (const actual of unidades) {
      if (actual.health <= 0 || !Number.isInteger(actual.x) || !Number.isInteger(actual.y) ||
        actual.x < 0 || actual.y < 0 || actual.x >= this.datos.mapa.width ||
        actual.y >= this.datos.mapa.height) continue;
      vivas.add(actual.id);
      let vista = this.vistas.get(actual.id);
      if (!vista || vista.tipo !== actual.unitType || vista.propietario !== actual.ownerOrder) {
        vista?.grupo.destroy(true);
        vista = this.crearVista(actual);
        this.vistas.set(actual.id, vista);
      }
      const destinoX = actual.x * TAM_CASILLA + 20;
      const destinoY = actual.y * TAM_CASILLA + 20;
      if (vista.x !== actual.x || vista.y !== actual.y) {
        this.tweens.killTweensOf(vista.grupo);
        if (animar) {
          this.reproducir(vista, 'move');
          this.tweens.add({ targets: vista.grupo, x: destinoX, y: destinoY,
            duration: 190, ease: 'Linear' });
        } else {
          vista.grupo.setPosition(destinoX, destinoY);
        }
        vista.x = actual.x;
        vista.y = actual.y;
      }
      vista.salud.setText(`${actual.health}`);
      vista.salud.setColor(actual.health <= 3 ? '#ff7675' : '#ffffff');
      vista.sprite.setAlpha(actual.movedThisTurn && actual.attackedThisTurn ? 0.78 : 1);
    }
    for (const [id, vista] of this.vistas) {
      if (!vivas.has(id)) {
        this.tweens.killTweensOf(vista.grupo);
        vista.grupo.destroy(true);
        this.vistas.delete(id);
      }
    }
    this.actualizarMarcado();
  }

  private crearVista(unidad: unidadservidor): VistaUnidad {
    const px = unidad.x * TAM_CASILLA + 20;
    const py = unidad.y * TAM_CASILLA + 20;
    const clave = this.claveUnidad(unidad);
    const grupo = this.add.container(px, py);
    let sprite: Phaser.GameObjects.Sprite;
    if (clave) {
      sprite = this.add.sprite(0, 0, clave);
    } else {
      // Fallback de textura sin romper la escena si aparece un tipo desconocido.
      if (!this.textures.exists('unidad-desconocida')) {
        const canvas = this.textures.createCanvas('unidad-desconocida', 40, 40);
        if (canvas) {
          const contexto = canvas.getContext();
          contexto.fillStyle = '#b77d7d';
          contexto.fillRect(5, 5, 30, 30);
          contexto.strokeStyle = '#ffffff';
          contexto.strokeRect(5, 5, 30, 30);
          canvas.refresh();
        }
      }
      sprite = this.add.sprite(0, 0, 'unidad-desconocida');
    }
    const salud = this.add.text(15, 13, `${unidad.health}`, {
      fontFamily: 'monospace', fontSize: '11px', color: '#ffffff',
      backgroundColor: '#14201a', stroke: '#14201a', strokeThickness: 1
    }).setOrigin(1, 1);
    grupo.add(sprite);
    if (!clave) grupo.add(this.add.text(0, -2, '?', { fontFamily: 'monospace',
      fontSize: '18px', color: '#ffffff' }).setOrigin(0.5));
    grupo.add(salud);
    this.capaUnidades.add(grupo);
    const vista = { grupo, sprite, salud, tipo: unidad.unitType,
      propietario: unidad.ownerOrder, x: unidad.x, y: unidad.y };
    this.reproducir(vista, 'idle');
    return vista;
  }

  /** Registra animaciones solo cuando sus PNG fueron precargados. */
  private reproducir(vista: VistaUnidad, accion: AnimacionUnidad): void {
    const faccion = this.faccion(vista.propietario);
    if (!faccion) return;
    const clave = `aura-${faccion}-${vista.tipo}-${accion}`;
    if (!this.anims.exists(clave)) {
      const frames = Array.from({ length: CUADROS[accion] }, (_, i) =>
        `anim-${faccion}-${vista.tipo}-${accion}-${String(i).padStart(2, '0')}`);
      if (!frames.every(frame => this.textures.exists(frame))) return;
      this.anims.create({ key: clave, frames: frames.map(key => ({ key })),
        frameRate: accion === 'idle' ? 2 : 12, repeat: accion === 'idle' ? -1 : 0 });
    }
    if (!this.anims.exists(clave)) return;
    vista.sprite.play(clave, true);
    if (accion !== 'idle') {
      vista.sprite.once(Phaser.Animations.Events.ANIMATION_COMPLETE, () => {
        if (vista.sprite.active) {
          const base = `unidad-${faccion}-${vista.tipo}`;
          if (this.textures.exists(base)) vista.sprite.setTexture(base);
          this.reproducir(vista, 'idle');
        }
      });
    }
  }

  /** Efectos de combate solo tras recibir AttackResultDTO confirmado. */
  mostrarCombate(resultado: resultadoataque): void {
    if (!this.sys.isActive()) return;
    const atacante = this.vistas.get(resultado.attackerUnitId);
    const objetivo = this.vistas.get(resultado.targetUnitId);
    if (atacante) this.reproducir(atacante, 'attack');
    if (objetivo) {
      this.reproducir(objetivo, resultado.targetDestroyed ? 'destroy' : 'hit');
      this.explosion(objetivo.grupo.x, objetivo.grupo.y);
    }
    if (atacante && resultado.attackerDestroyed) {
      this.reproducir(atacante, 'destroy');
      this.explosion(atacante.grupo.x, atacante.grupo.y);
    } else if (atacante && resultado.counterattackDamage > 0) {
      this.tweens.add({ targets: atacante.sprite, alpha: 0.4,
        duration: 85, yoyo: true, repeat: 1 });
    }
  }

  private explosion(x: number, y: number): void {
    const clave = 'aura-explosion';
    if (!this.anims.exists(clave)) {
      const frames = Array.from({ length: 6 }, (_, i) =>
        `efecto-explosion-${String(i).padStart(2, '0')}`);
      if (!frames.every(frame => this.textures.exists(frame))) return;
      this.anims.create({ key: clave, frames: frames.map(key => ({ key })),
        frameRate: 14, repeat: 0 });
    }
    const efecto = this.add.sprite(x, y, 'efecto-explosion-00').setDepth(9);
    efecto.once(Phaser.Animations.Events.ANIMATION_COMPLETE, () => efecto.destroy());
    efecto.play(clave);
  }

  /** Actualiza destinos confirmados por Spring Boot; no calcula reglas en Phaser. */
  marcarMovimientosDisponibles(casillas: casillaalcanzable[]): void {
    this.datos.movimientosDisponibles = [...casillas];
    this.dibujarMovimientosDisponibles();
  }

  /** La capa de destinos queda sobre el terreno y bajo las marcas de selección. */
  private dibujarMovimientosDisponibles(): void {
    if (!this.capaMovimientos || !this.sys.isActive()) return;
    this.capaMovimientos.clear();
    if (this.unidadMarcadaId === null) return;
    for (const destino of this.datos.movimientosDisponibles ?? []) {
      if (!Number.isInteger(destino.x) || !Number.isInteger(destino.y)
          || destino.x < 0 || destino.y < 0
          || destino.x >= this.datos.mapa.width || destino.y >= this.datos.mapa.height) continue;
      const px = destino.x * TAM_CASILLA;
      const py = destino.y * TAM_CASILLA;
      this.capaMovimientos.fillStyle(0x42ff92, 0.31);
      this.capaMovimientos.fillRoundedRect(px + 3, py + 3, TAM_CASILLA - 6, TAM_CASILLA - 6, 5);
      this.capaMovimientos.lineStyle(2, 0x52ffae, 0.95);
      this.capaMovimientos.strokeRoundedRect(px + 3, py + 3, TAM_CASILLA - 6, TAM_CASILLA - 6, 5);
      this.capaMovimientos.fillStyle(0xe3ffe9, 0.97);
      this.capaMovimientos.fillCircle(px + TAM_CASILLA / 2, py + TAM_CASILLA / 2, 3);
    }
  }

  /** Mantiene la selección visual separada de las validaciones del backend. */
  marcarUnidadSeleccionada(id: number | null): void {
    this.unidadMarcadaId = id;
    this.datos.unidadSeleccionadaId = id;
    if (id === null) this.datos.movimientosDisponibles = [];
    this.actualizarMarcado();
    this.dibujarMovimientosDisponibles();
  }

  private actualizarMarcado(): void {
    if (!this.capaSeleccion) return;
    this.capaSeleccion.clear();
    const vista = this.unidadMarcadaId !== null ? this.vistas.get(this.unidadMarcadaId) : null;
    if (!vista) return;
    const px = vista.x * TAM_CASILLA;
    const py = vista.y * TAM_CASILLA;
    this.capaSeleccion.fillStyle(0xffd84d, 0.15);
    this.capaSeleccion.fillRect(px + 2, py + 2, 36, 36);
    this.capaSeleccion.lineStyle(3, 0xffd84d, 1);
    this.capaSeleccion.strokeRect(px + 2, py + 2, 36, 36);
  }

  /** Mouse y táctil: solamente informan clics y coordenadas a Angular. */
  private activarMouse(): void {
    this.input.on('pointermove', (puntero: Phaser.Input.Pointer) => {
      const x = Math.floor(puntero.worldX / TAM_CASILLA);
      const y = Math.floor(puntero.worldY / TAM_CASILLA);
      this.resaltado.clear();
      if (x < 0 || y < 0 || x >= this.datos.mapa.width || y >= this.datos.mapa.height) {
        if (this.ultimaCasilla !== null) {
          this.ultimaCasilla = null;
          this.datos.alPasarSobre(null);
        }
        return;
      }
      this.resaltado.lineStyle(2, 0xffffff, 0.9);
      this.resaltado.strokeRect(x * TAM_CASILLA, y * TAM_CASILLA, TAM_CASILLA, TAM_CASILLA);
      const clave = `${x},${y}`;
      if (clave === this.ultimaCasilla) return;
      this.ultimaCasilla = clave;
      this.datos.alPasarSobre(this.datos.mapa.tiles.find(tile => tile.x === x && tile.y === y) ?? null);
    });
    this.input.on('pointerdown', (puntero: Phaser.Input.Pointer) => {
      if (puntero.button !== 0 || !this.datos.alClicCasilla) return;
      const x = Math.floor(puntero.worldX / TAM_CASILLA);
      const y = Math.floor(puntero.worldY / TAM_CASILLA);
      if (x >= 0 && y >= 0 && x < this.datos.mapa.width && y < this.datos.mapa.height) {
        this.datos.alClicCasilla(x, y);
      }
    });
    this.input.on('gameout', () => {
      this.resaltado.clear();
      this.ultimaCasilla = null;
      this.datos.alPasarSobre(null);
    });
  }
}
