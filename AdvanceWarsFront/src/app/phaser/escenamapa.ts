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
  jugadorEnTurno?: number;
  unidades?: unidadservidor[];
  unidadSeleccionadaId?: number | null;
  movimientosDisponibles?: casillaalcanzable[];
  alPasarSobre: (casilla: casilla | null) => void;
  alClicCasilla?: (x: number, y: number) => void;
  alCancelarSeleccion?: () => void;
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
  private animacionesPendientes = new Set<string>();
  private readonly indiceCasillas = new Map<string, casilla>();
  private arrastrandoCamara = false;
  private huboArrastreCamara = false;
  private inicioArrastreX = 0;
  private inicioArrastreY = 0;
  private anteriorMouseX = 0;
  private anteriorMouseY = 0;
  private zoomRelativo = 1;
  private readonly margenMapa = TAM_CASILLA / 2 + 4;

  /** Ajusta el mapa completo al área del canvas al 100%, sin recortar tropas de los extremos. */
  private ajustarCamaraAlTablero(): void {
    if (!this.datos) return;
    const camara = this.cameras.main;
    const anchoMapa = this.datos.mapa.width * TAM_CASILLA;
    const altoMapa = this.datos.mapa.height * TAM_CASILLA;
    if (anchoMapa <= 0 || altoMapa <= 0 || camara.width <= 0 || camara.height <= 0) return;

    // 100% es el encuadre completo, no una escala fija que recorta mapas grandes.
    const ajusteBase = Math.min(
      camara.width / (anchoMapa + 2 * this.margenMapa),
      camara.height / (altoMapa + 2 * this.margenMapa)
    );
    const zoomFinal = Math.max(0.1, ajusteBase * this.zoomRelativo);
    const centroX = camara.scrollX + camara.width / (2 * (camara.zoom || 1));
    const centroY = camara.scrollY + camara.height / (2 * (camara.zoom || 1));
    const margenX = Math.max(this.margenMapa, (camara.width / zoomFinal - anchoMapa) / 2);
    const margenY = Math.max(this.margenMapa, (camara.height / zoomFinal - altoMapa) / 2);
    camara.setBounds(-margenX, -margenY, anchoMapa + 2 * margenX, altoMapa + 2 * margenY);
    camara.setZoom(zoomFinal);
    if (this.zoomRelativo <= 1) {
      camara.centerOn(anchoMapa / 2, altoMapa / 2);
    } else {
      camara.centerOn(
        Phaser.Math.Clamp(centroX, 0, anchoMapa),
        Phaser.Math.Clamp(centroY, 0, altoMapa)
      );
    }
  }

  /** Zoom relativo al mapa encuadrado: 100% siempre vuelve a mostrarlo completo. */
  cambiarZoom(valor: number): void {
    if (!this.sys.isActive()) return;
    this.zoomRelativo = Phaser.Math.Clamp(valor, 0.8, 2);
    this.ajustarCamaraAlTablero();
  }

  /** Con el mapa completo visible, no desplaces la cámara hacia una esquina. */
  centrarEnJugador(orden: number): void {
    if (!this.sys.isActive()) return;
    if (this.zoomRelativo <= 1) {
      const { width, height } = this.datos.mapa;
      this.cameras.main.centerOn(width * TAM_CASILLA / 2, height * TAM_CASILLA / 2);
      return;
    }
    const tropa = this.datos.unidades?.find(u => u.ownerOrder === orden && u.health > 0);
    const base = this.datos.propiedades?.find(t => t.propertyType === 'HEADQUARTERS' && t.ownerOrder === orden);
    const punto = tropa ?? base;
    if (punto) this.cameras.main.centerOn(punto.x * TAM_CASILLA + 20, punto.y * TAM_CASILLA + 20);
  }

  constructor() { super('EscenaMapa'); }

  /** Recibe una instantánea del backend o del modo demostración. */
  init(datos: DatosEscena): void {
    this.datos = datos;
    this.ultimaCasilla = null;
    this.unidadMarcadaId = datos.unidadSeleccionadaId ?? null;
    this.vistas.clear();
    this.animacionesPendientes.clear();
    this.indiceCasillas.clear();
    for (const tile of datos.mapa.tiles) this.indiceCasillas.set(`${tile.x},${tile.y}`, tile);
    this.arrastrandoCamara = false;
    this.huboArrastreCamara = false;
    this.zoomRelativo = 1;
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
        for (let frame = 0; frame < 2; frame++) {
          const numero = String(frame).padStart(2, '0');
          this.cargarImagen(`edificio-anim-${faccion}-${propiedad}-${numero}`,
            `${RAIZ}animaciones/edificios/${faccion}/${propiedad}/idle_${numero}.png`);
        }
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
    for (let i = 0; i < 2; i++) {
      this.cargarImagen(`efecto-humo-${String(i).padStart(2, '0')}`,
        `${RAIZ}animaciones/efectos/humo_${String(i).padStart(2, '0')}.png`);
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
      if (tipo === 'INFANTRY') this.cargarInfanteriaIlustrada(faccion);
    }
  }

  /** Los 72 fotogramas ilustrados se cargan solo cuando existe infantería. */
  private cargarInfanteriaIlustrada(faccion: string): void {
    for (const accion of ['walk', 'fire', 'capture']) {
      for (let i = 0; i < 6; i++) {
        const numero = String(i).padStart(2, '0');
        this.cargarImagen(`ilustrada-${faccion}-${accion}-${numero}`,
          `${RAIZ}animaciones/infanteria_ilustrada/${faccion}/${accion}_${numero}.png`);
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
    this.ajustarCamaraAlTablero();
    this.scale.on(Phaser.Scale.Events.RESIZE, this.ajustarCamaraAlTablero, this);
    this.events.once(Phaser.Scenes.Events.SHUTDOWN, () => {
      this.scale.off(Phaser.Scale.Events.RESIZE, this.ajustarCamaraAlTablero, this);
    });
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
    this.centrarEnJugador(this.datos.jugadorEnTurno ?? 1);
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

  /** Los PNG y sus fotogramas ya fueron diseñados en 40 × 40: no deben reducirse a 24 px. */
  private ajustarTamanoSpriteUnidad(sprite: Phaser.GameObjects.Sprite): void {
    const original = sprite.texture.getSourceImage() as { width?: number; height?: number } | undefined;
    const ancho = original?.width || sprite.width || TAM_CASILLA;
    const alto = original?.height || sprite.height || TAM_CASILLA;
    const factor = TAM_CASILLA / Math.max(ancho, alto);
    sprite.setDisplaySize(Math.round(ancho * factor), Math.round(alto * factor));
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
        const edificio = this.add.sprite(px, py, clave);
        const animacion = `aura-edificio-${faccion}-${propiedad.propertyType}`;
        const frames = [0, 1].map(i =>
          `edificio-anim-${faccion}-${propiedad.propertyType}-${String(i).padStart(2, '0')}`);
        if (frames.every(k => this.textures.exists(k))) {
          if (!this.anims.exists(animacion)) this.anims.create({ key: animacion,
            frames: frames.map(key => ({ key })), frameRate: 1.2, repeat: -1 });
          edificio.play(animacion);
        }
        this.capaEdificios.add(edificio);
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

  /** Refleja capturas sin destruir las texturas ni reiniciar el mapa. */
  actualizarPropiedades(propiedades: casillatablero[]): void {
    this.datos.propiedades = propiedades;
    if (this.capaEdificios && this.sys.isActive()) this.dibujarPropiedades();
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
      this.cargarAnimacionesCuandoAparezca(actual);
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
      vista.salud.setText(actual.health >= 100 ? '' : `${Math.ceil(actual.health / 10)}`);
      vista.salud.setColor(actual.health <= 30 ? '#ff7675' : '#ffffff');
      vista.sprite.setAlpha(actual.movedThisTurn && actual.attackedThisTurn ? 0.78 : 1);
    }
    for (const [id, vista] of this.vistas) {
      if (!vivas.has(id)) {
        this.tweens.killTweensOf(vista.grupo);
        this.animarDestruccion(vista, animar);
        vista.grupo.destroy(true);
        this.vistas.delete(id);
      }
    }
    this.actualizarMarcado();
  }

  /** Al producir un tipo nuevo, sus secuencias se cargan sin reiniciar el tablero. */
  private cargarAnimacionesCuandoAparezca(unidad: unidadservidor): void {
    const faction = this.faccion(unidad.ownerOrder);
    if (!faction || !this.tipoConocido(unidad.unitType)) return;
    const id = `${faction}-${unidad.unitType}`;
    if (this.animacionesPendientes.has(id)) return;
    this.animacionesPendientes.add(id);
    let count = 0;
    for (const action of Object.keys(CUADROS) as AnimacionUnidad[]) {
      for (let i = 0; i < CUADROS[action]; i++) {
        const frame = String(i).padStart(2, '0');
        const key = `anim-${faction}-${unidad.unitType}-${action}-${frame}`;
        if (this.textures.exists(key)) continue;
        this.load.image(key, `${RAIZ}animaciones/unidades/${faction}/${unidad.unitType}/${action}_${frame}.png`);
        count++;
      }
    }
    if (unidad.unitType === 'INFANTRY') {
      for (const action of ['walk', 'fire', 'capture']) {
        for (let i = 0; i < 6; i++) {
          const frame = String(i).padStart(2, '0');
          const key = `ilustrada-${faction}-${action}-${frame}`;
          if (this.textures.exists(key)) continue;
          this.load.image(key, `${RAIZ}animaciones/infanteria_ilustrada/${faction}/${action}_${frame}.png`);
          count++;
        }
      }
    }
    if (count === 0) return;
    this.load.once(Phaser.Loader.Events.COMPLETE, () => {
      const view = this.vistas.get(unidad.id);
      if (view && view.sprite.active) this.reproducir(view, 'idle');
    });
    this.load.start();
  }

  /** Una copia efímera termina el efecto tras confirmar la destrucción. */
  private animarDestruccion(vista: VistaUnidad, animar: boolean): void {
    if (!animar) return;
    const faction = this.faccion(vista.propietario);
    if (!faction) return;
    const key = `aura-${faction}-${vista.tipo}-destroy`;
    if (!this.anims.exists(key)) {
      const frames = Array.from({ length: CUADROS.destroy }, (_, i) =>
        `anim-${faction}-${vista.tipo}-destroy-${String(i).padStart(2, '0')}`);
      if (!frames.every(frame => this.textures.exists(frame))) return;
      this.anims.create({ key, frames: frames.map(frame => ({ key: frame })), frameRate: 12, repeat: 0 });
    }
    const ghost = this.add.sprite(vista.grupo.x, vista.grupo.y, vista.sprite.texture.key).setDepth(11);
    this.ajustarTamanoSpriteUnidad(ghost);
    ghost.once(Phaser.Animations.Events.ANIMATION_COMPLETE, () => ghost.destroy());
    ghost.play(key);
    this.time.delayedCall(900, () => { if (ghost.active) ghost.destroy(); });
  }

  private crearVista(unidad: unidadservidor): VistaUnidad {
    const px = unidad.x * TAM_CASILLA + 20;
    const py = unidad.y * TAM_CASILLA + 20;
    const clave = this.claveUnidad(unidad);
    const grupo = this.add.container(px, py);
    // Una franja contrastada permite localizar cada ficha sobre agua/bosques.
    const faccion = this.faccion(unidad.ownerOrder);
    const indiceFaccion = FACCIONES.findIndex(f => f === faccion);
    const color = COLORES[indiceFaccion] ?? 0xffffff;
    grupo.add(this.add.rectangle(0, 16, 34, 5, color, 0.95));
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
    this.ajustarTamanoSpriteUnidad(sprite);
    sprite.on(Phaser.Animations.Events.ANIMATION_UPDATE, () => this.ajustarTamanoSpriteUnidad(sprite));
    const salud = this.add.text(15, 13, unidad.health >= 100 ? '' : `${Math.ceil(unidad.health / 10)}`, {
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
    const ilustrada = vista.tipo === 'INFANTRY' && accion !== 'idle' && accion !== 'hit' && accion !== 'destroy';
    const variante = ilustrada ? (accion === 'move' ? 'walk' : 'fire') : accion;
    const clave = ilustrada ? `aura-ilustrada-${faccion}-${variante}` : `aura-${faccion}-${vista.tipo}-${accion}`;
    if (!this.anims.exists(clave)) {
      const frames = Array.from({ length: ilustrada ? 6 : CUADROS[accion] }, (_, i) =>
        ilustrada ? `ilustrada-${faccion}-${variante}-${String(i).padStart(2, '0')}` :
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
          this.ajustarTamanoSpriteUnidad(vista.sprite);
          this.reproducir(vista, 'idle');
        }
      });
    }
  }

  /** Reproduce captura visual sin cambiar las reglas del backend. */
  mostrarCaptura(id: number): void {
    const unidad = this.vistas.get(id);
    if (!unidad) return;
    const faccion = this.faccion(unidad.propietario);
    if (unidad.tipo === 'INFANTRY' && faccion) {
      const frames = Array.from({ length: 6 }, (_, i) =>
        `ilustrada-${faccion}-capture-${String(i).padStart(2, '0')}`);
      if (frames.every(key => this.textures.exists(key))) {
        const clave = `aura-ilustrada-${faccion}-capture`;
        if (!this.anims.exists(clave)) this.anims.create({ key: clave,
          frames: frames.map(key => ({ key })), frameRate: 10, repeat: 0 });
        unidad.sprite.play(clave, true);
        unidad.sprite.once(Phaser.Animations.Events.ANIMATION_COMPLETE, () => {
          if (unidad.sprite.active) this.reproducir(unidad, 'idle');
        });
        return;
      }
    }
    this.tweens.add({ targets: unidad.sprite, alpha: 0.4, duration: 130, yoyo: true, repeat: 2 });
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
    // El humo aprovecha los otros dos PNG del paquete de efectos.
    const framesHumo = ['efecto-humo-00', 'efecto-humo-01'];
    if (framesHumo.every(key => this.textures.exists(key))) {
      const humo = this.add.sprite(x, y, framesHumo[0]).setDepth(9).setAlpha(0.8);
      const nombre = 'aura-humo';
      if (!this.anims.exists(nombre)) this.anims.create({ key: nombre,
        frames: framesHumo.map(key => ({ key })), frameRate: 5, repeat: 1 });
      humo.play(nombre);
      humo.once(Phaser.Animations.Events.ANIMATION_COMPLETE, () => humo.destroy());
    }
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
      this.capaMovimientos.fillStyle(0x4eacff, 0.34);
      this.capaMovimientos.fillRect(px + 2, py + 2, TAM_CASILLA - 4, TAM_CASILLA - 4);
      this.capaMovimientos.lineStyle(2, 0xc9edff, 0.95);
      this.capaMovimientos.strokeRect(px + 2, py + 2, TAM_CASILLA - 4, TAM_CASILLA - 4);
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

  /** Un único marco azul, siempre dentro de una sola casilla de 40 × 40. */
  private actualizarMarcado(): void {
    if (!this.capaSeleccion) return;
    this.capaSeleccion.clear();
    const vista = this.unidadMarcadaId !== null ? this.vistas.get(this.unidadMarcadaId) : null;
    if (!vista) return;
    const px = vista.x * TAM_CASILLA;
    const py = vista.y * TAM_CASILLA;
    const x0 = px + 2;
    const y0 = py + 2;
    const x1 = px + TAM_CASILLA - 2;
    const y1 = py + TAM_CASILLA - 2;
    const esquina = 8;
    this.capaSeleccion.lineStyle(2, 0x139df5, 1);
    this.capaSeleccion.strokeRect(x0, y0, TAM_CASILLA - 4, TAM_CASILLA - 4);
    this.capaSeleccion.lineStyle(3, 0x64dfff, 1);
    this.capaSeleccion.lineBetween(x0, y0 + esquina, x0, y0);
    this.capaSeleccion.lineBetween(x0, y0, x0 + esquina, y0);
    this.capaSeleccion.lineBetween(x1 - esquina, y0, x1, y0);
    this.capaSeleccion.lineBetween(x1, y0, x1, y0 + esquina);
    this.capaSeleccion.lineBetween(x0, y1 - esquina, x0, y1);
    this.capaSeleccion.lineBetween(x0, y1, x0 + esquina, y1);
    this.capaSeleccion.lineBetween(x1 - esquina, y1, x1, y1);
    this.capaSeleccion.lineBetween(x1, y1 - esquina, x1, y1);
  }

  /** El puntero del sistema no se dibuja como un sprite enorme dentro del mapa. */
  private mostrarCursorSobreCasilla(x: number, y: number): void {
    this.resaltado.clear();
    const seleccion = this.unidadMarcadaId !== null ? this.vistas.get(this.unidadMarcadaId) : null;
    if (seleccion?.x === x && seleccion?.y === y) return;
    this.resaltado.lineStyle(2, 0xffe38a, 0.95);
    this.resaltado.strokeRect(x * TAM_CASILLA + 2, y * TAM_CASILLA + 2,
      TAM_CASILLA - 4, TAM_CASILLA - 4);
  }

  /** La cámara se desplaza con clic derecho sostenido; sin controles de teclado. */
  private activarMouse(): void {
    this.input.mouse?.disableContextMenu();
    this.input.setDefaultCursor('crosshair');
    this.input.on('pointerdown', (puntero: Phaser.Input.Pointer) => {
      if (puntero.button === 2) {
        this.arrastrandoCamara = this.zoomRelativo > 1;
        if (!this.arrastrandoCamara) {
          this.datos.alCancelarSeleccion?.();
          return;
        }
        this.huboArrastreCamara = false;
        this.inicioArrastreX = puntero.x;
        this.inicioArrastreY = puntero.y;
        this.anteriorMouseX = puntero.x;
        this.anteriorMouseY = puntero.y;
        return;
      }
      if (puntero.button !== 0 || !this.datos.alClicCasilla) return;
      const punto = this.cameras.main.getWorldPoint(puntero.x, puntero.y);
      const x = Math.floor(punto.x / TAM_CASILLA);
      const y = Math.floor(punto.y / TAM_CASILLA);
      if (x >= 0 && y >= 0 && x < this.datos.mapa.width && y < this.datos.mapa.height) {
        this.datos.alClicCasilla(x, y);
      }
    });
    this.input.on('pointermove', (puntero: Phaser.Input.Pointer) => {
      if (this.arrastrandoCamara) {
        if (!puntero.isDown) {
          this.arrastrandoCamara = false;
          this.huboArrastreCamara = false;
          return;
        }
        if (Math.hypot(puntero.x - this.inicioArrastreX,
          puntero.y - this.inicioArrastreY) > 4) {
          this.huboArrastreCamara = true;
        }
        if (this.huboArrastreCamara) {
          const camara = this.cameras.main;
          camara.scrollX -= (puntero.x - this.anteriorMouseX) / camara.zoom;
          camara.scrollY -= (puntero.y - this.anteriorMouseY) / camara.zoom;
          this.resaltado.clear();
          if (this.ultimaCasilla !== null) {
            this.ultimaCasilla = null;
            this.datos.alPasarSobre(null);
          }
        }
        this.anteriorMouseX = puntero.x;
        this.anteriorMouseY = puntero.y;
        return;
      }
      const punto = this.cameras.main.getWorldPoint(puntero.x, puntero.y);
      const x = Math.floor(punto.x / TAM_CASILLA);
      const y = Math.floor(punto.y / TAM_CASILLA);
      if (x < 0 || y < 0 || x >= this.datos.mapa.width || y >= this.datos.mapa.height) {
        this.resaltado.clear();
        if (this.ultimaCasilla !== null) {
          this.ultimaCasilla = null;
          this.datos.alPasarSobre(null);
        }
        return;
      }
      const clave = `${x},${y}`;
      if (clave === this.ultimaCasilla) return;
      this.ultimaCasilla = clave;
      this.mostrarCursorSobreCasilla(x, y);
      this.datos.alPasarSobre(this.indiceCasillas.get(clave) ?? null);
    });
    this.input.on('pointerup', (puntero: Phaser.Input.Pointer) => {
      if (puntero.button !== 2 || !this.arrastrandoCamara) return;
      const fueArrastre = this.huboArrastreCamara;
      this.arrastrandoCamara = false;
      this.huboArrastreCamara = false;
      if (!fueArrastre) this.datos.alCancelarSeleccion?.();
    });
    this.input.on('pointerupoutside', () => {
      this.arrastrandoCamara = false;
      this.huboArrastreCamara = false;
    });
    this.input.on('gameout', () => {
      this.resaltado.clear();
      this.ultimaCasilla = null;
      this.datos.alPasarSobre(null);
    });
  }
}
