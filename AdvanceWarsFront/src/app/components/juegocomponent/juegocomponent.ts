import { Component, AfterViewInit, ChangeDetectorRef, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
  proporcion = '4 / 3';
  seleccion: { x: number; y: number; terreno?: terreno } | null = null;
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
          if (this.modoDemo) this.dibujar(Juegocomponent.demoMap(r.config));
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
  /** Genera un tablero local para la demostración. */
  private static demoMap(config: configuracionmapa): tablero {
    const tiles: tablero['tiles'] = [];
    for (let y = 0; y < config.height; y++)
      for (let x = 0; x < config.width; x++) {
        let terrain = 'PLAIN';
        if (y === Math.floor(config.height / 2)) terrain = 'ROAD';
        else if ((x * 5 + y * 11) % 25 < 3) terrain = 'WATER';
        else if ((x * 7 + y * 3) % 19 < 4) terrain = 'FOREST';
        else if ((x * 13 + y * 7) % 37 < 3) terrain = 'MOUNTAIN';
        const hq =
          x === 1 && y === 1 ? 1 : x === config.width - 2 && y === config.height - 2 ? 2 : null;
        tiles.push({ x, y, terrain, propertyType: hq ? 'HEADQUARTERS' : null, ownerOrder: hq });
      }
    return { gameId: 0, width: config.width, height: config.height, tiles };
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
    } else this.phaser.scene.getScene('EscenaMapa')?.scene.restart(datos);
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
  /** Libera los recursos y suscripciones del componente. */
  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.phaser?.destroy(true);
    this.phaser = undefined;
  }
}
