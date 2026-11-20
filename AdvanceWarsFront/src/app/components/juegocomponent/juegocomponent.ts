
import { Component, AfterViewInit, ChangeDetectorRef, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import {
  Component,
  AfterViewInit,
  ChangeDetectorRef,
  OnDestroy,
  OnInit,
  inject,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';

import Phaser from 'phaser';
import { forkJoin, Subscription } from 'rxjs';

import { juegoservice } from '../../services/juegoservice';
import { tableroservice } from '../../services/tableroservice';
import { ReporteService } from '../../services/reporte.service';
import { EscenaMapa, DatosEscena, TAM_CASILLA } from '../../phaser/escenamapa';
import { casilla, configuracionmapa, terreno, mapa, tipoterreno } from '../../models/mapa';
import { tablero, partidaresumen, unidadservidor } from '../../models/tablero';
import { unidad as unidadcatalogo, resultadoataque } from '../../models/unidad';

/** Controla el tablero Phaser y las acciones solicitadas a Spring Boot. */
@Component({
  selector: 'app-juegocomponent',
  standalone: false,
  templateUrl: './juegocomponent.html',
  styleUrl: './juegocomponent.css',
})
export class Juegocomponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly ruta = inject(ActivatedRoute);
  private readonly catalogos = inject(juegoservice);
  private readonly api = inject(tableroservice);
  private readonly pdf = inject(ReporteService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly subs = new Subscription();

  private phaser?: Phaser.Game;
  private suscripcionRecargaUnidades?: Subscription;
  private vistaLista = false;
  private mapaPendiente: tablero | null = null;
  private datosEscenaActuales: DatosEscena | null = null;
  private destruido = false;

  readonly modoDemo = Boolean(this.ruta.snapshot.data['demo']);
  readonly id = Number(this.ruta.snapshot.paramMap.get('id')) || 0;

  configuracion: configuracionmapa | null = null;
  terrenos: terreno[] = [];
  catalogoUnidades: unidadcatalogo[] = [];

  cargando = true;
  error = '';
  mensaje = '';
  partida: partidaresumen | null = null;
  unidades: unidadservidor[] = [];
  unidadActiva: unidadservidor | null = null;
  resultadoCombate: resultadoataque | null = null;

  movimientoEnCurso = false;
  ataqueEnCurso = false;
  recargandoUnidades = false;

  semilla = 12345;
  jugadores = 4;
  proporcion = '4 / 3';
  seleccion: { x: number; y: number; terreno?: terreno } | null = null;

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
  colores = ['#e74b40', '#366cb1', '#3f9e4d', '#e0b020'];
  ganancias: Record<number, number> = {};
  constructor(
    private ruta: ActivatedRoute,
    private catalogos: juegoservice,
    private api: tableroservice,
    private pdf: ReporteService,
    private cdr: ChangeDetectorRef,
  ) {
    this.modoDemo = Boolean(this.ruta.snapshot.data['demo']);
    this.id = Number(this.ruta.snapshot.paramMap.get('id')) || 0;


  /** Impide acciones simultáneas mientras se espera una respuesta del servidor. */
  get accionEnCurso(): boolean {
    return this.movimientoEnCurso || this.ataqueEnCurso || this.recargandoUnidades;

  }

  /** Carga los catálogos JSON y prepara el tablero. */
  ngOnInit(): void {
    this.cargando = true;
    this.error = '';
    this.subs.add(
      forkJoin({
        terrenos: this.catalogos.getTerrenos(),
        config: this.catalogos.getConfiguracionMapa(),
        unidadesCatalogo: this.catalogos.getUnidades(),
      }).subscribe({
        next: (respuesta) => {
          if (this.destruido) return;
          this.terrenos = respuesta.terrenos;
          this.configuracion = respuesta.config;
          this.catalogoUnidades = respuesta.unidadesCatalogo;

          if (this.modoDemo) this.generarDemo();
          else this.cargarMapa();

          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          if (this.destruido) return;
          this.cargando = false;
          this.error = Juegocomponent.obtenerMensajeError(
            error,
            'No se pudieron cargar los catálogos JSON.',
          );
          this.cdr.detectChanges();
        },
      }),
    );
  }

  /** Dibuja un mapa recibido antes de que Angular creara la vista. */
  ngAfterViewInit(): void {
    this.vistaLista = true;
    if (this.mapaPendiente) {
      const pendiente = this.mapaPendiente;
      this.mapaPendiente = null;
      this.dibujar(pendiente);
    }
  }

  /** Devuelve el nombre legible de la unidad definido en Unidades.json. */
  nombreUnidad(tipo: string): string {
    const definicion = this.catalogoUnidades.find((unidad) => unidad.unitType === tipo);
    return definicion?.name ?? tipo;
  }

  /** Consulta el mapa desde Spring Boot sin modificar las reglas de juego. */
  cargarMapa(): void {
    if (this.modoDemo || !this.id || this.destruido || this.accionEnCurso) return;

    this.cargando = true;
    this.error = '';
    this.cdr.markForCheck();
    this.subs.add(
      this.api.getTablero(this.id).subscribe({
        next: (tableroRecibido) => {
          if (this.destruido) return;
          this.dibujar(tableroRecibido);
          this.cargarDatos();
          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          if (this.destruido) return;
          this.cargando = false;
          this.error = Juegocomponent.obtenerMensajeError(error, 'No se pudo cargar el mapa.');
          this.cdr.detectChanges();
        },
      }),
    );
  }

  /** Consulta el último GameDTO y la lista de unidades del servidor. */
  cargarDatos(): void {
    if (this.modoDemo || !this.id || this.destruido || this.accionEnCurso) return;

    this.error = '';
    this.subs.add(
      forkJoin({
        partida: this.api.getPartida(this.id),
        unidades: this.api.getUnidades(this.id),
      }).subscribe({
        next: (respuesta) => {
          if (this.destruido) return;
          this.partida = respuesta.partida;
          this.unidades = respuesta.unidades;
          this.actualizarUnidadesEscena();
          this.mensaje = 'Partida y unidades actualizadas desde Spring Boot.';
          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          if (this.destruido) return;
          this.error = Juegocomponent.obtenerMensajeError(
            error,
            'No se pudieron consultar los datos de la partida.',
          );
          this.cdr.detectChanges();
        },
      }),
    );
  }

  /** Recarga las unidades al terminar el turno, sin reconstruir el mapa. */
  recargarUnidades(): void {
    if (this.modoDemo || !this.id || this.destruido || this.movimientoEnCurso || this.ataqueEnCurso)
      return;

    this.suscripcionRecargaUnidades?.unsubscribe();
    this.recargandoUnidades = true;
    this.error = '';
    this.establecerUnidadActiva(null);
    this.cdr.detectChanges();

    const peticion = this.api.getUnidades(this.id).subscribe({
      next: (unidadesRecibidas) => {
        if (this.destruido) return;
        this.unidades = unidadesRecibidas;
        this.actualizarUnidadesEscena();
        this.recargandoUnidades = false;
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        if (this.destruido) return;
        this.recargandoUnidades = false;
        this.error = Juegocomponent.obtenerMensajeError(
          error,
          'No se pudieron actualizar las unidades.',
        );
        this.cdr.detectChanges();
      },
    });

    this.suscripcionRecargaUnidades = peticion;
    this.subs.add(peticion);
  }

  /** Conserva la selección solamente si pertenece al turno actual y puede moverse. */
  private comprobarUnidadActiva(): void {
    if (!this.unidadActiva) return;
    const partidaActual = this.partida;
    const actualizada = this.unidades.find((unidad) => unidad.id === this.unidadActiva?.id);

    if (
      !partidaActual ||
      !actualizada ||
      actualizada.health <= 0 ||
      actualizada.movedThisTurn ||
      actualizada.ownerOrder !== partidaActual.currentPlayerOrder
    ) {
      this.unidadActiva = null;
      return;
    }
    this.unidadActiva = actualizada;
  }

  /** Obtiene la escena Phaser que dibuja el mapa. */
  private obtenerEscenaMapa(): EscenaMapa | null {
    if (!this.phaser) return null;
    const escena = this.phaser.scene
      .getScenes(false)
      .find((actual) => actual.scene.key === 'EscenaMapa');
    return escena instanceof EscenaMapa ? escena : null;
  }

  /** Redibuja unidades y selección con los datos actuales del servidor. */
  private actualizarUnidadesEscena(): void {
    if (this.destruido) return;
    this.comprobarUnidadActiva();
    const datos = this.datosEscenaActuales;
    if (!datos) return;

    datos.unidades = [...this.unidades];
    datos.unidadSeleccionadaId = this.unidadActiva?.id ?? null;
    const escena = this.obtenerEscenaMapa();
    if (!escena || !escena.sys.isActive()) return;

    escena.actualizarUnidades(this.unidades);
    escena.marcarUnidadSeleccionada(this.unidadActiva?.id ?? null);
  }

  /** Cambia únicamente el marco de la unidad seleccionada en Phaser. */
  private actualizarSeleccionEscena(): void {
    const datos = this.datosEscenaActuales;
    if (!datos || this.destruido) return;
    const unidadId = this.unidadActiva?.id ?? null;
    datos.unidadSeleccionadaId = unidadId;
    const escena = this.obtenerEscenaMapa();
    if (escena && escena.sys.isActive()) escena.marcarUnidadSeleccionada(unidadId);
  }

  /** Guarda la selección del jugador y solicita su resaltado gráfico. */
  private establecerUnidadActiva(unidad: unidadservidor | null): void {
    this.unidadActiva = unidad;
    this.actualizarSeleccionEscena();
  }

  /** Solicita al backend mover la unidad activa a una casilla vacía. */
  private solicitarMovimiento(x: number, y: number): void {
    if (this.destruido || this.modoDemo || this.accionEnCurso) return;
    const unidad = this.unidadActiva;
    const partidaActual = this.partida;

    if (!unidad || !partidaActual) {
      this.error = 'Selecciona una unidad disponible.';
      this.cdr.detectChanges();
      return;
    }
    if (partidaActual.status !== 'IN_PROGRESS') {
      this.error = 'La partida no está en curso.';
      this.cdr.detectChanges();
      return;
    }
    if (
      unidad.health <= 0 ||
      unidad.movedThisTurn ||
      unidad.ownerOrder !== partidaActual.currentPlayerOrder
    ) {
      this.establecerUnidadActiva(null);
      this.error = 'La unidad seleccionada no puede moverse.';
      this.cdr.detectChanges();
      return;
    }

    this.movimientoEnCurso = true;
    this.resultadoCombate = null;
    this.error = '';
    this.mensaje = 'Procesando movimiento...';
    this.cdr.detectChanges();

    this.subs.add(
      this.api
        .mover(this.id, unidad.id, x, y, {
          currentRound: partidaActual.currentRound,
          currentPlayerOrder: partidaActual.currentPlayerOrder,
        })
        .subscribe({
          next: (actualizada) => {
            if (this.destruido) return;
            this.movimientoEnCurso = false;
            if (actualizada.id !== unidad.id) {
              this.error = 'El servidor devolvió una unidad diferente.';
              this.mensaje = '';
              this.cdr.detectChanges();
              return;
            }
            // Nunca se usan x/y de la solicitud para mover el sprite.
            this.unidades = this.unidades.map((actual) =>
              actual.id === actualizada.id ? actualizada : actual,
            );
            this.establecerUnidadActiva(null);
            this.actualizarUnidadesEscena();
            this.error = '';
            this.mensaje = `Unidad #${actualizada.id} movida a (${actualizada.x}, ${actualizada.y}).`;
            this.cdr.detectChanges();
          },
          error: (error: unknown) => {
            if (this.destruido) return;
            this.movimientoEnCurso = false;
            // Una respuesta de error no cambia la posición de ninguna unidad.
            this.error = Juegocomponent.obtenerMensajeError(error, 'Movimiento rechazado.');
            this.mensaje = '';
            this.cdr.detectChanges();
          },
        }),
    );
  }

  /** Solicita un ataque; las bajas y los daños los calcula Spring Boot. */
  private solicitarAtaque(objetivo: unidadservidor): void {
    if (this.destruido || this.modoDemo || this.accionEnCurso) return;
    const atacante = this.unidadActiva;
    const partidaActual = this.partida;

    if (!atacante || !partidaActual) {
      this.error = 'Selecciona una unidad atacante.';
      this.cdr.detectChanges();
      return;
    }
    if (partidaActual.status !== 'IN_PROGRESS') {
      this.error = 'La partida no está en curso.';
      this.cdr.detectChanges();
      return;
    }
    if (
      atacante.health <= 0 ||
      atacante.attackedThisTurn ||
      atacante.ownerOrder !== partidaActual.currentPlayerOrder
    ) {
      this.establecerUnidadActiva(null);
      this.error = 'La unidad seleccionada no puede atacar.';
      this.cdr.detectChanges();
      return;
    }
    if (objetivo.health <= 0 || objetivo.ownerOrder === atacante.ownerOrder) {
      this.error = 'Selecciona una unidad enemiga válida.';
      this.cdr.detectChanges();
      return;
    }

    this.ataqueEnCurso = true;
    this.error = '';
    this.resultadoCombate = null;
    this.mensaje = `Unidad #${atacante.id} atacando a unidad #${objetivo.id}...`;
    this.cdr.detectChanges();

    this.subs.add(
      this.api
        .atacar(this.id, atacante.id, objetivo.id, {
          currentRound: partidaActual.currentRound,
          currentPlayerOrder: partidaActual.currentPlayerOrder,
        })
        .subscribe({
          next: (resultado: resultadoataque) => {
            if (this.destruido) return;
            this.resultadoCombate = resultado;
            const destruccion =
              (resultado.targetDestroyed ? ' Unidad enemiga destruida.' : '') +
              (resultado.attackerDestroyed ? ' Unidad atacante destruida.' : '');
            this.mensaje =
              `Ataque confirmado. Daño: -${resultado.damageDealt}. ` +
              `Contraataque: -${resultado.counterattackDamage}.` +
              destruccion;

            // Aplicamos solamente valores de daño confirmados por Spring Boot.
            this.unidades = this.unidades
              .filter(
                (unidad) =>
                  !(unidad.id === resultado.attackerUnitId && resultado.attackerDestroyed) &&
                  !(unidad.id === resultado.targetUnitId && resultado.targetDestroyed),
              )
              .map((unidad) => {
                if (unidad.id === resultado.attackerUnitId) {
                  return { ...unidad, health: resultado.attackerHealth };
                }
                if (unidad.id === resultado.targetUnitId) {
                  return { ...unidad, health: resultado.targetHealth };
                }
                return unidad;
              });

            this.establecerUnidadActiva(null);
            this.actualizarUnidadesEscena();
            this.cdr.detectChanges();

            // La lista completa del back es la fuente final de verdad.
            this.subs.add(
              this.api.getUnidades(this.id).subscribe({
                next: (actualizadas) => {
                  if (this.destruido) return;
                  this.unidades = actualizadas;
                  this.actualizarUnidadesEscena();
                  this.ataqueEnCurso = false;
                  this.cdr.detectChanges();
                },
                error: (error: unknown) => {
                  if (this.destruido) return;
                  this.ataqueEnCurso = false;
                  this.error =
                    'El ataque fue confirmado, pero no se pudieron recargar las unidades. ' +
                    Juegocomponent.obtenerMensajeError(error, 'Actualiza la partida.');
                  this.cdr.detectChanges();
                },
              }),
            );
          },
          error: (error: unknown) => {
            if (this.destruido) return;
            this.ataqueEnCurso = false;
            // No se altera el tablero si Spring Boot rechaza el ataque.
            this.error = Juegocomponent.obtenerMensajeError(error, 'Ataque rechazado.');
            this.mensaje = '';
            this.cdr.detectChanges();
          },
        }),
    );
  }

  /** Obtiene el mensaje de error del DTO de Spring Boot. */
  private static obtenerMensajeError(error: unknown, alternativo: string): string {
    if (error instanceof HttpErrorResponse) {
      const respuesta: unknown = error.error;
      if (
        respuesta !== null &&
        typeof respuesta === 'object' &&
        'message' in respuesta &&
        typeof respuesta.message === 'string' &&
        respuesta.message.trim()
      )
        return respuesta.message;
      if (error.status === 0) {
        return 'No se pudo conectar con Spring Boot. Comprueba que esté encendido.';
      }
      return `${alternativo} HTTP ${error.status}.`;
    }
    if (error instanceof Error) return error.message;
    return alternativo;
  }

  /** Genera un nuevo mapa local para el modo demostración. */
  private generarDemo(): void {
    if (!this.modoDemo || !this.configuracion || this.destruido) return;
    this.cargando = true;
    this.error = '';
    this.mensaje = '';
    this.partida = null;
    this.unidades = [];
    this.unidadActiva = null;
    this.resultadoCombate = null;
    this.dibujar(this.demoMap(this.configuracion));
    this.cdr.markForCheck();
  }

  /** Genera un mapa determinista a partir de la semilla. */
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

    /** Calcula el siguiente valor seudoaleatorio. */
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
        if (ownerOrder !== null) terrain = 'PLAIN';
        tiles.push({
          x,
          y,
          terrain,
          propertyType: ownerOrder !== null ? 'HEADQUARTERS' : null,
          ownerOrder,
        });
      }
    }
    return { gameId: 0, width: ancho, height: alto, tiles };
  }

  /** Muestra un mapa en Phaser y configura los callbacks de interacción. */
  private dibujar(tableroRecibido: tablero): void {
    if (this.destruido) return;
    if (!this.vistaLista) {
      this.mapaPendiente = tableroRecibido;
      return;
    }
    this.cargando = false;
    this.proporcion = `${tableroRecibido.width} / ${tableroRecibido.height}`;
    this.seleccion = null;
    this.unidadActiva = null;

    const mapaEscena: mapa = {
      width: tableroRecibido.width,
      height: tableroRecibido.height,
      tiles: tableroRecibido.tiles.map((actual) => ({
        x: actual.x,
        y: actual.y,
        terrainType: actual.terrain as tipoterreno,
      })),
    };
    const bases = tableroRecibido.tiles
      .filter((actual) => actual.propertyType === 'HEADQUARTERS')
      .sort((primera, segunda) => (primera.ownerOrder ?? 0) - (segunda.ownerOrder ?? 0))
      .map((actual) => ({ x: actual.x, y: actual.y }));

    const datos: DatosEscena = {
      mapa: mapaEscena,
      terrenos: this.terrenos,
      bases,
      unidades: [...this.unidades],
      unidadSeleccionadaId: null,
      alPasarSobre: (actual: casilla | null) => this.alPasarSobre(actual),
      alClicCasilla: (x: number, y: number) => this.alClicCasilla(x, y),
    };
    this.datosEscenaActuales = datos;
    this.cdr.markForCheck();

    const anchoPixeles = tableroRecibido.width * TAM_CASILLA;
    const altoPixeles = tableroRecibido.height * TAM_CASILLA;
    if (!this.phaser) {
      this.phaser = new Phaser.Game({
        type: Phaser.AUTO,
        parent: 'contenedor-juego',
        width: anchoPixeles,
        height: altoPixeles,
        backgroundColor: '#14201a',
        scale: { mode: Phaser.Scale.FIT, autoCenter: Phaser.Scale.NO_CENTER },
        render: { pixelArt: true },
        scene: [],
      });
      this.phaser.scene.add('EscenaMapa', EscenaMapa, true, datos);
    } else {
      this.phaser.scale.resize(anchoPixeles, altoPixeles);
      this.phaser.scene.getScene('EscenaMapa').scene.restart(datos);
    }
    this.cdr.markForCheck();
  }

  /** Actualiza únicamente el inspector de casillas bajo el cursor. */
  private alPasarSobre(actual: casilla | null): void {
    if (this.destruido) return;
    if (!actual) {
      if (this.seleccion) {
        this.seleccion = null;
        this.cdr.markForCheck();
      }
      return;
    }
    if (this.seleccion?.x === actual.x && this.seleccion?.y === actual.y) return;
    this.seleccion = {
      x: actual.x,
      y: actual.y,
      terreno: this.terrenos.find((item) => item.terrainType === actual.terrainType),
    };
    this.cdr.markForCheck();
  }

  /** Selecciona una unidad, mueve a una casilla vacía o ataca a un enemigo. */
  private alClicCasilla(x: number, y: number): void {
    if (this.destruido || this.accionEnCurso) return;
    if (this.modoDemo) {
      this.mensaje = `Casilla (${x}, ${y}) del mapa de demostración.`;
      this.cdr.detectChanges();
      return;
    }
    if (!this.partida) {
      this.error = 'Todavía no se han cargado los datos de la partida.';
      this.cdr.detectChanges();
      return;
    }

    const encontrada = this.unidades.find(
      (unidad) => unidad.x === x && unidad.y === y && unidad.health > 0,
    );
    if (!encontrada) {
      if (this.unidadActiva) {
        this.solicitarMovimiento(x, y);
        return;
      }
      this.mensaje = `Casilla (${x}, ${y}) sin unidad.`;
      this.cdr.detectChanges();
      return;
    }
    if (encontrada.ownerOrder !== this.partida.currentPlayerOrder) {
      if (this.unidadActiva) {
        this.solicitarAtaque(encontrada);
        return;
      }
      this.mensaje = 'Selecciona primero una unidad propia para atacar.';
      this.cdr.detectChanges();
      return;
    }
    if (encontrada.movedThisTurn) {
      this.mensaje = `La unidad #${encontrada.id} ya se movió.`;
      this.cdr.detectChanges();
      return;
    }
    if (this.unidadActiva?.id === encontrada.id) {
      this.establecerUnidadActiva(null);
      this.mensaje = `Unidad #${encontrada.id} deseleccionada.`;
      this.cdr.detectChanges();
      return;
    }

    this.establecerUnidadActiva(encontrada);
    this.error = '';
    this.resultadoCombate = null;
    this.mensaje =
      `Unidad ${this.nombreUnidad(encontrada.unitType)} ` +
      `(#${encontrada.id}) seleccionada. Clic en casilla vacía para mover ` +
      'o en enemigo para atacar.';
    this.cdr.detectChanges();
  }

  /** Obtiene la unidad bajo el cursor del inspector existente. */
  get unidadSeleccionada(): unidadservidor | null {
    if (!this.seleccion) return null;
    return (
      this.unidades.find(
        (unidad) => unidad.x === this.seleccion?.x && unidad.y === this.seleccion?.y,
      ) ?? null
    );
  }

  /** Regenera el mapa demo con una nueva semilla. */
  nuevoMapa(): void {
    if (!this.modoDemo) return;
    this.semilla = Math.floor(Math.random() * 1000000);
    this.generarDemo();
  }

  /** Aplica la semilla introducida en el mapa de demostración. */
  usarSemilla(): void {
    if (!this.modoDemo) return;
    const numero = Number(this.semilla);
    if (!Number.isFinite(numero)) {
      this.error = 'Introduce una semilla válida.';
      this.cdr.markForCheck();
      return;
    }
    this.semilla = Math.floor(numero);
    this.generarDemo();
  }

  /** Cambia el número de jugadores del mapa local. */
  fijarJugadores(cantidad: number): void {
    if (!this.modoDemo || !Number.isInteger(cantidad) || cantidad < 2 || cantidad > 4) return;
    this.jugadores = cantidad;
    this.generarDemo();
  }

  /** Consulta el costo local de un terreno para el inspector. */
  readonly obtenerCosto = (terrenoActual: terreno, tipo: string): string => {
    const costo = terrenoActual.movementCost[tipo];
    return costo === undefined || costo === null || costo === 0 ? 'No pasa' : String(costo);
  };

  /** Descarga el reporte PDF desde el servicio ya existente. */
  descargarPDF(): void {
    if (this.modoDemo || !this.id) return;
    this.error = '';
    this.subs.add(
      this.pdf.obtener(this.id).subscribe({
        next: (blob) => {
          if (this.destruido) return;
          try {
            this.pdf.guardarArchivo(blob, this.id);
            this.mensaje = 'PDF descargado.';
          } catch (error) {
            this.error = Juegocomponent.obtenerMensajeError(error, 'No se pudo guardar el PDF.');
          }
          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          if (this.destruido) return;
          this.error = Juegocomponent.obtenerMensajeError(error, 'No se pudo descargar el PDF.');
          this.cdr.detectChanges();
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

    const dineroAntes: Record<number, number> = {};
    for (const j of this.partida.players) {
      dineroAntes[j.turnOrder] = j.money;
    }

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
        this.ganancias = {};
        for (const j of p.players) {
          const diferencia = j.money - (dineroAntes[j.turnOrder] ?? j.money);
          if (diferencia > 0) {
            this.ganancias[j.turnOrder] = diferencia;
          }
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

  /** Muestra "Turno de X" y lo que ganó durante 2 segundos. */
  private mostrarAviso(): void {
    this.avisoTurno = `TURNO DE ${this.jugadorEnTurno}`;
    setTimeout(() => {
      this.avisoTurno = '';
      this.ganancias = {};
      this.cdr.detectChanges();
    }, 2000);
  }

  /** Libera los recursos y suscripciones del componente. */

  /** Libera peticiones, suscripciones y recursos gráficos al salir. */

  ngOnDestroy(): void {
    this.destruido = true;
    this.suscripcionRecargaUnidades?.unsubscribe();
    this.subs.unsubscribe();
    this.mapaPendiente = null;
    this.datosEscenaActuales = null;
    this.unidadActiva = null;
    this.movimientoEnCurso = false;
    this.ataqueEnCurso = false;
    this.recargandoUnidades = false;
    if (this.phaser) {
      this.phaser.destroy(true);
      this.phaser = undefined;
    }
  }
}
