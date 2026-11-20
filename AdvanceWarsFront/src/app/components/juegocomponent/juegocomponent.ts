
import {
  Component,
  OnInit,
  AfterViewInit,
  OnDestroy,
  ChangeDetectorRef
} from '@angular/core';

import { ActivatedRoute } from '@angular/router';
import Phaser from 'phaser';
import { forkJoin, Subscription } from 'rxjs';

import { juegoservice } from '../../services/juegoservice';
import { tableroservice } from '../../services/tableroservice';
import { ReporteService } from '../../services/reporte.service';

import {
  EscenaMapa,
  DatosEscena,
  TAM_CASILLA
} from '../../phaser/escenamapa';

import {
  casilla,
  configuracionmapa,
  terreno,
  mapa,
  tipoterreno
} from '../../models/mapa';

import {
  tablero,
  partidaresumen,
  unidadservidor
} from '../../models/tablero';

@Component({
  selector: 'app-juegocomponent',
  standalone: false,
  templateUrl: './juegocomponent.html',
  styleUrl: './juegocomponent.css'
})
export class Juegocomponent
  implements OnInit, AfterViewInit, OnDestroy {

  private phaser?: Phaser.Game;
  private subs = new Subscription();
  private viewLista = false;
  private pendingMap: tablero | null = null;
  private destruido = false;

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

  seleccion: {
    x: number;
    y: number;
    terreno?: terreno;
  } | null = null;

  readonly tiposMovimiento = [
    { codigo: 'FOOT', nombre: 'Infantería' },
    { codigo: 'MECH', nombre: 'Mecanizada' },
    { codigo: 'TREAD', nombre: 'Oruga' },
    { codigo: 'WHEEL', nombre: 'Ruedas' },
    { codigo: 'AIR', nombre: 'Aire' },
    { codigo: 'SHIP', nombre: 'Barco' }
  ];

  constructor(
    private ruta: ActivatedRoute,
    private catalogos: juegoservice,
    private api: tableroservice,
    private pdf: ReporteService,
    private cdr: ChangeDetectorRef
  ) {
    this.modoDemo = Boolean(
      this.ruta.snapshot.data['demo']
    );

    this.id = Number(
      this.ruta.snapshot.paramMap.get('id')
    ) || 0;
  }

  ngOnInit(): void {

    this.cargando = true;
    this.error = '';

    this.subs.add(
      forkJoin({
        terrenos: this.catalogos.getTerrenos(),
        config: this.catalogos.getConfiguracionMapa()
      }).subscribe({

        next: (respuesta) => {

          this.terrenos = respuesta.terrenos;
          this.configuracion = respuesta.config;

          if (this.modoDemo) {
            this.generarDemo();
          } else {
            this.cargarMapa();
          }

          this.cdr.markForCheck();
        },

        error: (error) => {

          this.cargando = false;

          this.error =
            `No se pudieron cargar los catálogos (HTTP ${error.status || 0}).`;

          this.cdr.markForCheck();
        }

      })
    );
  }

  ngAfterViewInit(): void {

    this.viewLista = true;

    if (this.pendingMap) {

      const mapaPendiente = this.pendingMap;
      this.pendingMap = null;

      this.dibujar(mapaPendiente);
    }
  }

  cargarMapa(): void {

    if (this.modoDemo || !this.id) {
      return;
    }

    this.cargando = true;
    this.error = '';
    this.cdr.markForCheck();

    this.subs.add(
      this.api.getTablero(this.id).subscribe({

        next: (tableroRecibido) => {

          this.dibujar(tableroRecibido);
          this.cargarDatos();
          this.cdr.markForCheck();
        },

        error: (error) => {

          this.cargando = false;

          this.error =
            `No se pudo consultar el mapa (HTTP ${error.status || 0}).`;

          this.cdr.markForCheck();
        }

      })
    );
  }

  cargarDatos(): void {

    if (this.modoDemo || !this.id) {
      return;
    }

    this.subs.add(
      forkJoin({
        partida: this.api.getPartida(this.id),
        unidades: this.api.getUnidades(this.id)
      }).subscribe({

        next: (respuesta) => {

          this.partida = respuesta.partida;
          this.unidades = respuesta.unidades;

          this.mensaje =
            'Datos de partida y unidades actualizados.';

          this.cdr.markForCheck();
        },

        error: (error) => {

          this.mensaje =
            `No se pudieron consultar los datos de la partida (HTTP ${error.status || 0}).`;

          this.cdr.markForCheck();
        }

      })
    );
  }

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

  private demoMap(
    config: configuracionmapa
  ): tablero {

    const tiles: tablero['tiles'] = [];

    const ancho = config.width;
    const alto = config.height;

    const posiciones = [
      { x: 1, y: 1 },
      { x: ancho - 2, y: alto - 2 },
      { x: ancho - 2, y: 1 },
      { x: 1, y: alto - 2 }
    ];

    let estado = Math.floor(this.semilla) >>> 0;

    const aleatorio = (): number => {

      estado = (
        Math.imul(1664525, estado) + 1013904223
      ) >>> 0;

      return estado / 4294967296;
    };

    for (let y = 0; y < alto; y++) {

      for (let x = 0; x < ancho; x++) {

        let terrain: tipoterreno = 'PLAIN';

        const valor = aleatorio();

        if (
          x === Math.floor(ancho / 2) ||
          y === Math.floor(alto / 2)
        ) {
          terrain = 'ROAD';
        } else if (valor < 0.12) {
          terrain = 'WATER';
        } else if (valor < 0.30) {
          terrain = 'FOREST';
        } else if (valor < 0.39) {
          terrain = 'MOUNTAIN';
        }

        const indiceBase = posiciones.findIndex(
          (posicion, indice) =>
            indice < this.jugadores &&
            posicion.x === x &&
            posicion.y === y
        );

        const ownerOrder =
          indiceBase >= 0 ? indiceBase + 1 : null;

        if (ownerOrder !== null) {
          terrain = 'PLAIN';
        }

        tiles.push({
          x,
          y,
          terrain,
          propertyType:
            ownerOrder !== null ? 'HEADQUARTERS' : null,
          ownerOrder
        });
      }
    }

    return {
      gameId: 0,
      width: ancho,
      height: alto,
      tiles
    };
  }

  private dibujar(tableroRecibido: tablero): void {

    if (this.destruido) {
      return;
    }

    if (!this.viewLista) {

      this.pendingMap = tableroRecibido;
      return;
    }

    this.cargando = false;

    this.proporcion =
      `${tableroRecibido.width} / ${tableroRecibido.height}`;

    this.seleccion = null;

    const mapaEscena: mapa = {
      width: tableroRecibido.width,
      height: tableroRecibido.height,
      tiles: tableroRecibido.tiles.map(c => ({
        x: c.x,
        y: c.y,
        terrainType: c.terrain as tipoterreno
      }))
    };

    const bases = tableroRecibido.tiles
      .filter(c => c.propertyType === 'HEADQUARTERS')
      .sort(
        (a, b) =>
          (a.ownerOrder ?? 0) - (b.ownerOrder ?? 0)
      )
      .map(c => ({
        x: c.x,
        y: c.y
      }));

    const datos: DatosEscena = {
      mapa: mapaEscena,
      terrenos: this.terrenos,
      bases,
      alPasarSobre: (c: casilla | null) =>
        this.alPasarSobre(c)
    };

    this.cdr.markForCheck();

    if (!this.phaser) {

      this.phaser = new Phaser.Game({

        type: Phaser.AUTO,
        parent: 'contenedor-juego',

        width:
          tableroRecibido.width * TAM_CASILLA,

        height:
          tableroRecibido.height * TAM_CASILLA,

        backgroundColor: '#14201a',

        scale: {
          mode: Phaser.Scale.FIT,
          autoCenter: Phaser.Scale.NO_CENTER
        },

        render: {
          pixelArt: true
        },

        scene: []
      });

      this.phaser.scene.add(
        'EscenaMapa',
        EscenaMapa,
        true,
        datos
      );

    } else {

      this.phaser.scene
        .getScene('EscenaMapa')
        .scene.restart(datos);
    }

    this.cdr.markForCheck();
  }

  private alPasarSobre(
    c: casilla | null
  ): void {

    if (!c) {

      if (this.seleccion !== null) {

        this.seleccion = null;
        this.cdr.markForCheck();
      }

      return;
    }

    if (
      this.seleccion?.x === c.x &&
      this.seleccion?.y === c.y
    ) {
      return;
    }

    const terrenoEncontrado = this.terrenos.find(
      t => t.terrainType === c.terrainType
    );

    this.seleccion = {
      x: c.x,
      y: c.y,
      terreno: terrenoEncontrado
    };

    this.cdr.markForCheck();
  }

  get unidadSeleccionada(): unidadservidor | null {

    if (!this.seleccion) {
      return null;
    }

    return this.unidades.find(
      u =>
        u.x === this.seleccion!.x &&
        u.y === this.seleccion!.y
    ) ?? null;
  }

  nuevoMapa(): void {

    if (!this.modoDemo) {
      return;
    }

    this.semilla = Math.floor(
      Math.random() * 1000000
    );

    this.generarDemo();
  }

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

  fijarJugadores(cantidad: number): void {

    if (!this.modoDemo) {
      return;
    }

    if (
      !Number.isInteger(cantidad) ||
      cantidad < 2 ||
      cantidad > 4
    ) {
      return;
    }

    this.jugadores = cantidad;

    this.generarDemo();
  }

  obtenerCosto(
    t: terreno,
    tipo: string
  ): string {

    const costo = t.movementCost[tipo];

    if (
      costo === undefined ||
      costo === null ||
      costo === 0
    ) {
      return 'No pasa';
    }

    return String(costo);
  }

  descargarPDF(): void {

    if (this.modoDemo || !this.id) {
      return;
    }

    this.error = '';
    this.cdr.markForCheck();

    this.subs.add(
      this.pdf.obtener(this.id).subscribe({

        next: (blob) => {

          try {

            this.pdf.guardarArchivo(blob, this.id);

            this.mensaje = 'PDF descargado correctamente.';

          } catch (error) {

            this.error = error instanceof Error
              ? error.message
              : 'No se pudo descargar el PDF.';
          }

          this.cdr.markForCheck();
        },

        error: (error) => {

          this.error =
            `Reporte no disponible (HTTP ${error.status || 0}).`;

          this.cdr.markForCheck();
        }

      })
    );
  }

  ngOnDestroy(): void {

    this.destruido = true;

    this.subs.unsubscribe();

    this.pendingMap = null;

    if (this.phaser) {

      this.phaser.destroy(true);
      this.phaser = undefined;
    }
  }
}
