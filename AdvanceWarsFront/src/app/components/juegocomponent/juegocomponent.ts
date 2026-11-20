import { Component, AfterViewInit, ChangeDetectorRef, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import Phaser from 'phaser';
import { forkJoin, Subscription } from 'rxjs';
import { juegoservice } from '../../services/juegoservice';
import { tableroservice } from '../../services/tableroservice';
import { EscenaMapa, DatosEscena, TAM_CASILLA } from '../../phaser/escenamapa';
import { casilla, configuracionmapa, terreno, mapa, tipoterreno } from '../../models/mapa';
import { tablero, partidaresumen, unidadservidor } from '../../models/tablero';
import { ReporteService } from '../../services/reporte.service';
/** Muestra el tablero Phaser y consulta datos de la partida. */
@Component({
  selector: 'app-juegocomponent',
  standalone: false,
  templateUrl: './juegocomponent.html',
  styleUrl: './juegocomponent.css',
})
export class Juegocomponent implements OnInit, AfterViewInit, OnDestroy {
  private phaser?: Phaser.Game;
  private subs = new Subscription();
  private viewLista = false;
  private pendingMap: tablero | null = null;
  readonly modoDemo: boolean;
  readonly id: number;
  configuracion: configuracionmapa | null = null;
  terrenos: terreno[] = [];
  cargando = true;
  error = '';
  mensaje = '';
  partida: partidaresumen | null = null;
  unidades: unidadservidor[] = [];
  semilla = 12345;
  jugadores = 4;
  proporcion = '4 / 3';
  readonly tiposMovimiento = [
    { codigo: 'FOOT', nombre: 'Infantería' },
    { codigo: 'MECH', nombre: 'Mecanizada' },
    { codigo: 'TREAD', nombre: 'Oruga' },
    { codigo: 'WHEEL', nombre: 'Ruedas' },
    { codigo: 'AIR', nombre: 'Aire' },
    { codigo: 'SHIP', nombre: 'Barco' },
  ];
  seleccion: { x: number; y: number; terreno?: terreno } | null = null;
  private router = inject(Router);
  avisoTurno = '';
  terminando = false;
  constructor(
    private ruta: ActivatedRoute,
    private catalogos: juegoservice,
    private api: tableroservice,
    private pdf: ReporteService,
    private cdr: ChangeDetectorRef,
  ) {
    this.modoDemo = Boolean(this.ruta.snapshot.data['demo']);
    this.id = Number(this.ruta.snapshot.paramMap.get('id')) || 0;
  }

  /** Inicializa y carga los datos de la pantalla. */
  ngOnInit(): void {
    this.subs.add(
      forkJoin({
        terrenos: this.catalogos.getTerrenos(),
        config: this.catalogos.getConfiguracionMapa(),
      }).subscribe({
        next: (r) => {
          this.terrenos = r.terrenos;
          this.configuracion = r.config;
          if (this.modoDemo) this.generarDemo();
          else this.cargarMapa();
          this.cdr.markForCheck();
        },
        error: (e) => {
          this.cargando = false;
          this.error = `No se cargaron catálogos JSON (HTTP ${e.status || 0}).`;
          this.cdr.markForCheck();
        },
      }),
    );
  }

  /** Inicializa los recursos gráficos al estar disponible la vista. */
  ngAfterViewInit(): void {
    this.viewLista = true;
    if (this.pendingMap) {
      const pending = this.pendingMap;
      this.pendingMap = null;
      this.dibujar(pending);
    }
  }

  /** Recupera el mapa de la partida registrada en el servidor. */
  cargarMapa(): void {
    if (this.modoDemo || !this.id) return;
    this.cargando = true;
    this.error = '';
    this.cdr.markForCheck();
    this.subs.add(
      this.api.getTablero(this.id).subscribe({
        next: (t) => {
          this.dibujar(t);
          this.cargarDatos();
          this.cdr.markForCheck();
        },
        error: (e) => {
          this.cargando = false;
          this.error = `No se pudo consultar el mapa (HTTP ${e.status || 0}).`;
          this.cdr.markForCheck();
        },
      }),
    );
  }

  /** Consulta los datos complementarios para actualizar la vista. */
  cargarDatos(): void {
    if (this.modoDemo || !this.id) return;
    this.subs.add(
      forkJoin({
        partida: this.api.getPartida(this.id),
        unidades: this.api.getUnidades(this.id),
      }).subscribe({
        next: (r) => {
          this.partida = r.partida;
          this.unidades = r.unidades;
          this.mensaje = 'Datos de partida y unidades recibidos desde Spring Boot.';
          this.cdr.markForCheck();
        },
        error: (e) => {
          this.mensaje = `No se pudieron consultar las unidades (HTTP ${e.status || 0}).`;
          this.cdr.markForCheck();
        },
      }),
    );
  }

  /** Genera y muestra el tablero local sin realizar peticiones al backend. */
  private generarDemo(): void {
    if (!this.modoDemo || !this.configuracion) {
      return;
    }

    this.cargando = true;
    this.error = '';
    this.mensaje = '';

    const mapaDemo = this.demoMap(this.configuracion);

    this.dibujar(mapaDemo);
    this.cdr.markForCheck();
  }

  /** Construye el mapa a partir de la semilla y la cantidad de jugadores. */
  private demoMap(config: configuracionmapa): tablero {
    const tiles: tablero['tiles'] = [];

    const ancho = config.width;
    const alto = config.height;

    const posiciones = [
      { x: 1, y: 1 },
      { x: ancho - 2, y: alto - 2 },
      { x: ancho - 2, y: 1 },
      { x: 1, y: alto - 2 },
    ];

    let estado = Math.floor(this.semilla) >>> 0;

    /** Devuelve el siguiente número seudoaleatorio para la semilla seleccionada. */
    const aleatorio = (): number => {
      estado = (Math.imul(1664525, estado) + 1013904223) >>> 0;

      return estado / 4294967296;
    };

    for (let y = 0; y < alto; y++) {
      for (let x = 0; x < ancho; x++) {
        let terrain: tipoterreno = 'PLAIN';

        const valor = aleatorio();

        if (x === Math.floor(ancho / 2) || y === Math.floor(alto / 2)) {
          terrain = 'ROAD';
        } else if (valor < 0.12) {
          terrain = 'WATER';
        } else if (valor < 0.3) {
          terrain = 'FOREST';
        } else if (valor < 0.39) {
          terrain = 'MOUNTAIN';
        }

        const indiceBase = posiciones.findIndex(
          (posicion, indice) => indice < this.jugadores && posicion.x === x && posicion.y === y,
        );

        const ownerOrder = indiceBase >= 0 ? indiceBase + 1 : null;

        if (ownerOrder !== null) {
          terrain = 'PLAIN';
        }

        tiles.push({
          x,
          y,
          terrain,
          propertyType: ownerOrder !== null ? 'HEADQUARTERS' : null,
          ownerOrder,
        });
      }
    }

    return {
      gameId: 0,
      width: ancho,
      height: alto,
      tiles,
    };
  }

  /** Representa el mapa recibido con una escena Phaser. */
  private dibujar(t: tablero): void {
    if (!this.viewLista) {
      this.pendingMap = t;
      return;
    }
    this.cargando = false;
    this.proporcion = `${t.width} / ${t.height}`;
    this.seleccion = null;
    this.cdr.markForCheck();
    const mapaEscena: mapa = {
      width: t.width,
      height: t.height,
      tiles: t.tiles.map((c) => ({ x: c.x, y: c.y, terrainType: c.terrain as tipoterreno })),
    };
    const bases = t.tiles
      .filter((c) => c.propertyType === 'HEADQUARTERS')
      .sort((a, b) => (a.ownerOrder ?? 0) - (b.ownerOrder ?? 0))
      .map((c) => ({ x: c.x, y: c.y }));
    const datos: DatosEscena = {
      mapa: mapaEscena,
      terrenos: this.terrenos,
      bases,
      alPasarSobre: (c) => this.alPasarSobre(c),
    };
    if (!this.phaser) {
      this.phaser = new Phaser.Game({
        type: Phaser.AUTO,
        parent: 'contenedor-juego',
        width: t.width * TAM_CASILLA,
        height: t.height * TAM_CASILLA,
        backgroundColor: '#14201a',
        scale: { mode: Phaser.Scale.FIT, autoCenter: Phaser.Scale.NO_CENTER },
        render: { pixelArt: true },
        scene: [],
      });
      this.phaser.scene.add('EscenaMapa', EscenaMapa, true, datos);
    } else {
      this.phaser.scale.resize(t.width * TAM_CASILLA, t.height * TAM_CASILLA);
      this.phaser.scene.getScene('EscenaMapa')?.scene.restart(datos);
    }
  }

  /** Actualiza la casilla resaltada por el cursor. */
  private alPasarSobre(c: casilla | null): void {
    if (!c) {
      if (this.seleccion) {
        this.seleccion = null;
        this.cdr.markForCheck();
      }
      return;
    }
    if (this.seleccion?.x === c.x && this.seleccion?.y === c.y) return;
    this.seleccion = {
      x: c.x,
      y: c.y,
      terreno: this.terrenos.find((t) => t.terrainType === c.terrainType),
    };
    this.cdr.markForCheck();
  }
  get unidadSeleccionada(): unidadservidor | null {
    const seleccion = this.seleccion;
    if (!seleccion) return null;
    return this.unidades.find((u) => u.x === seleccion.x && u.y === seleccion.y) ?? null;
  }

  /** Genera una nueva semilla y vuelve a construir el mapa local. */
  nuevoMapa(): void {
    if (!this.modoDemo) {
      return;
    }

    this.semilla = Math.floor(Math.random() * 1000000);

    this.generarDemo();
  }

  /** Aplica la semilla indicada por el usuario, si es válida. */
  usarSemilla(): void {
    if (!this.modoDemo) {
      return;
    }

    const numero = Number(this.semilla);

    if (!Number.isFinite(numero)) {
      this.error = 'Introduce una semilla válida.';
      this.cdr.markForCheck();
      return;
    }

    this.semilla = Math.floor(numero);

    this.generarDemo();
  }

  /** Cambia el número de jugadores de la demostración, entre dos y cuatro. */
  fijarJugadores(cantidad: number): void {
    if (!this.modoDemo) {
      return;
    }

    if (!Number.isInteger(cantidad) || cantidad < 2 || cantidad > 4) {
      return;
    }

    this.jugadores = cantidad;

    this.generarDemo();
  }

  /** Calcula el costo de movimiento de una unidad sobre un terreno. */
  readonly obtenerCosto = (terrenoActual: terreno, tipo: string): string => {
    const costo = terrenoActual.movementCost[tipo];
    return costo === undefined || costo === null || costo === 0 ? 'No pasa' : String(costo);
  };

  /** Recupera y guarda el reporte de la partida. */
  descargarPDF(): void {
    if (this.modoDemo || !this.id) return;
    this.subs.add(
      this.pdf.obtener(this.id).subscribe({
        next: (blob) => {
          try {
            this.pdf.guardarArchivo(blob, this.id);
            this.mensaje = 'PDF descargado.';
          } catch (e) {
            this.error = e instanceof Error ? e.message : 'No se pudo abrir el PDF.';
          }
          this.cdr.markForCheck();
        },
        error: (e) => {
          this.error = `Reporte no disponible (HTTP ${e.status || 0}).`;
          this.cdr.markForCheck();
        },
      }),
    );
  }

  /** Nombre del jugador que tiene el turno ahora. */
  get jugadorEnTurno(): string {
    if (!this.partida) return '';
    const actual = this.partida.players.find((j) => j.turnOrder === this.partida?.currentPlayerOrder);
    return actual ? actual.name : '';
  }

  /** Le pide al back terminar el turno actual. */
  terminarTurno(): void {
    if (!this.partida) return;
    this.terminando = true;
    this.error = '';
    const turno = {
      currentRound: this.partida.currentRound,
      currentPlayerOrder: this.partida.currentPlayerOrder,
    };
    this.api.finTurno(this.id, turno).subscribe({
      next: (p) => {
        this.partida = p;
        this.terminando = false;
        if (p.status === 'FINISHED') {
          this.router.navigate(['/partidas', this.id, 'resultados']);
          return;
        }
        this.mostrarAviso();
        this.cargarDatos();
        this.cdr.detectChanges();
      },
      error: (e) => {
        this.terminando = false;
        this.error = e.error?.message || 'No se pudo terminar el turno';
        this.cargarDatos();
        this.cdr.detectChanges();
      },
    });
  }

  /** Muestra "Turno de X" encima del tablero durante 2 segundos. */
  private mostrarAviso(): void {
    this.avisoTurno = `TURNO DE ${this.jugadorEnTurno}`;
    setTimeout(() => {
      this.avisoTurno = '';
      this.cdr.detectChanges();
    }, 2000);
  }

  /** Libera los recursos y suscripciones del componente. */
  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.phaser?.destroy(true);
    this.phaser = undefined;
  }
}
