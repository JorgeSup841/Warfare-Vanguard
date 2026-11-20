import { Component, AfterViewInit, ChangeDetectorRef, OnDestroy, OnInit, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import Phaser from 'phaser';
import { forkJoin, Subscription, switchMap, map } from 'rxjs';

import { juegoservice } from '../../services/juegoservice';
import { tableroservice } from '../../services/tableroservice';
import { ReporteService } from '../../services/reporte.service';
import { EscenaMapa, DatosEscena, TAM_CASILLA } from '../../phaser/escenamapa';
import { casilla, configuracionmapa, terreno, mapa, tipoterreno } from '../../models/mapa';
import { tablero, partidaresumen, unidadservidor, casillatablero, turnoactual, casillaalcanzable } from '../../models/tablero';
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
  private readonly router = inject(Router);
  private readonly catalogos = inject(juegoservice);
  private readonly api = inject(tableroservice);
  private readonly pdf = inject(ReporteService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly subs = new Subscription();

  private phaser?: Phaser.Game;
  private suscripcionRecargaUnidades?: Subscription;
  private suscripcionMovimientos?: Subscription;
  private vistaLista = false;
  private mapaPendiente: tablero | null = null;
  private datosEscenaActuales: DatosEscena | null = null;
  private tableroActual: tablero | null = null;
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
  capturando = false;
  produciendo = false;
  recargandoUnidades = false;
  consultandoMovimientos = false;
  movimientosDisponibles: casillaalcanzable[] = [];

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

  fabricaActiva: casillatablero | null = null;
  avisoTurno = '';
  terminando = false;
  colores = ['#e74b40', '#366cb1', '#3f9e4d', '#e0b020'];
  ganancias: Record<number, number> = {};
  tiendaAbierta = false;
  readonly tienda = [
    { tipo: 'INFANTRY' as const, nombre: 'Infantería', costo: 1000, detalle: 'Barata. Puede capturar edificios.' },
    { tipo: 'TANK' as const, nombre: 'Tanque', costo: 7000, detalle: 'Fuerte y rápido. No captura.' },
  ];

  /** Impide acciones simultáneas mientras se espera una respuesta del servidor. */
  get accionEnCurso(): boolean {
    return this.movimientoEnCurso || this.ataqueEnCurso || this.recargandoUnidades ||
      this.terminando || this.capturando || this.produciendo;

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

  /** Carga partida, mapa y unidades para la primera precarga gráfica. */
  cargarMapa(): void {
    if (this.modoDemo || !this.id || this.destruido || this.accionEnCurso) return;
    this.cargando = true;
    this.error = '';
    this.cdr.markForCheck();
    this.subs.add(
      this.api.getPartida(this.id).pipe(
        switchMap(partidaActual => forkJoin({
          tableroRecibido: this.api.getTablero(this.id),
          unidadesRecibidas: this.api.getUnidades(this.id)
        }).pipe(map(datos => ({ ...datos, partidaActual }))))
      ).subscribe({
        next: ({ partidaActual, tableroRecibido, unidadesRecibidas }) => {
          if (this.destruido) return;
          this.partida = partidaActual;
          this.unidades = unidadesRecibidas;
          this.dibujar(tableroRecibido);
          this.mensaje = 'Partida sincronizada con Spring Boot.';
          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          if (this.destruido) return;
          this.cargando = false;
          this.error = Juegocomponent.obtenerMensajeError(error, 'No se pudo cargar la partida.');
          this.cdr.detectChanges();
        }
      })
    );
  }

  /** Primero confirma GameDTO y después descarga las unidades del mismo turno. */
  cargarDatos(): void {
    if (this.modoDemo || !this.id || this.destruido || this.accionEnCurso) return;
    this.error = '';
    this.subs.add(
      this.api.getPartida(this.id).pipe(
        switchMap(partidaActual => this.api.getUnidades(this.id).pipe(
          map(unidades => ({ partidaActual, unidades }))))
      ).subscribe({
        next: ({ partidaActual, unidades }) => {
          if (this.destruido) return;
          this.partida = partidaActual;
          this.unidades = unidades;
          this.actualizarUnidadesEscena();
          this.mensaje = 'Partida y unidades actualizadas desde Spring Boot.';
          this.cdr.detectChanges();
        },
        error: (error: unknown) => {
          if (this.destruido) return;
          this.error = Juegocomponent.obtenerMensajeError(error,
            'No se pudieron consultar los datos de la partida.');
          this.cdr.detectChanges();
        }
      })
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
      (actualizada.movedThisTurn && actualizada.attackedThisTurn) ||
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
    datos.movimientosDisponibles = this.unidadActiva ? [...this.movimientosDisponibles] : [];
    const escena = this.obtenerEscenaMapa();
    if (!escena || !escena.sys.isActive()) return;

    escena.actualizarUnidades(this.unidades);
    escena.marcarUnidadSeleccionada(this.unidadActiva?.id ?? null);
    escena.marcarMovimientosDisponibles(datos.movimientosDisponibles ?? []);
  }

  /** Cambia únicamente el marco de la unidad seleccionada en Phaser. */
  private actualizarSeleccionEscena(): void {
    const datos = this.datosEscenaActuales;
    if (!datos || this.destruido) return;
    const unidadId = this.unidadActiva?.id ?? null;
    datos.unidadSeleccionadaId = unidadId;
    const escena = this.obtenerEscenaMapa();
    if (escena && escena.sys.isActive()) {
      escena.marcarUnidadSeleccionada(unidadId);
      escena.marcarMovimientosDisponibles(datos.movimientosDisponibles ?? []);
    }
  }

  /** Seleccionar otra unidad cancela resultados anteriores y consulta nuevos destinos. */
  private establecerUnidadActiva(unidad: unidadservidor | null): void {
    this.suscripcionMovimientos?.unsubscribe();
    this.suscripcionMovimientos = undefined;
    this.consultandoMovimientos = false;
    this.movimientosDisponibles = [];
    if (this.datosEscenaActuales) this.datosEscenaActuales.movimientosDisponibles = [];
    this.unidadActiva = unidad;
    this.actualizarSeleccionEscena();
    if (!unidad || this.modoDemo || !this.id || this.partida?.status !== 'IN_PROGRESS') return;

    this.consultandoMovimientos = true;
    this.cdr.markForCheck();
    const peticion = this.api.movimientosDisponibles(this.id, unidad.id).subscribe({
      next: (casillas) => {
        if (this.destruido || this.unidadActiva?.id !== unidad.id) return;
        this.consultandoMovimientos = false;
        this.movimientosDisponibles = casillas;
        if (this.datosEscenaActuales) this.datosEscenaActuales.movimientosDisponibles = [...casillas];
        const escena = this.obtenerEscenaMapa();
        if (escena && escena.sys.isActive()) escena.marcarMovimientosDisponibles(casillas);
        this.cdr.markForCheck();
      },
      error: (error: unknown) => {
        if (this.destruido || this.unidadActiva?.id !== unidad.id) return;
        this.consultandoMovimientos = false;
        this.movimientosDisponibles = [];
        this.error = Juegocomponent.obtenerMensajeError(error,
          'No se pudieron consultar los destinos de movimiento.');
        this.cdr.markForCheck();
      }
    });
    this.suscripcionMovimientos = peticion;
    this.subs.add(peticion);
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
    if (this.consultandoMovimientos) {
      this.mensaje = 'Consultando los movimientos disponibles...';
      this.cdr.detectChanges();
      return;
    }
    if (!this.movimientosDisponibles.some(casilla => casilla.x === x && casilla.y === y)) {
      this.mensaje = `La casilla (${x}, ${y}) no está marcada como destino permitido.`;
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

            // La animación representa únicamente el ataque que confirmó el servidor.
            this.obtenerEscenaMapa()?.mostrarCombate(resultado);
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
              this.api.getPartida(this.id).pipe(
                switchMap(partidaActual => this.api.getUnidades(this.id).pipe(
                  map(actualizadas => ({ partidaActual, actualizadas }))))
              ).subscribe({
                next: ({ partidaActual, actualizadas }) => {
                  if (this.destruido) return;
                  this.partida = partidaActual;
                  this.unidades = actualizadas;
                  this.actualizarUnidadesEscena();
                  this.ataqueEnCurso = false;
                  if (partidaActual.status === 'FINISHED') {
                    void this.router.navigate(['/partidas', this.id, 'resultados']);
                  }
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
    this.fabricaActiva = null;
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
    this.fabricaActiva = null;
    this.tableroActual = tableroRecibido;

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
      propiedades: tableroRecibido.tiles,
      faccionesPorOrden: Object.fromEntries((this.partida?.players ?? []).map(j =>
        [j.turnOrder, j.factionCode ?? null])),
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
      const fabrica = this.tableroActual?.tiles.find(tile =>
        tile.x === x && tile.y === y && tile.propertyType === 'FACTORY' &&
        tile.ownerOrder === this.partida?.currentPlayerOrder && tile.propertyId != null);
      this.fabricaActiva = fabrica ?? null;
      this.tiendaAbierta = Boolean(fabrica);
      this.mensaje = fabrica
        ? `Fábrica propia en (${x}, ${y}). Elige qué comprar en la tienda.`
        : `Casilla (${x}, ${y}) sin unidad.`;
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
    if (encontrada.movedThisTurn && encontrada.attackedThisTurn) {
      this.mensaje = `La unidad #${encontrada.id} ya gastó sus acciones.`;
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
    this.fabricaActiva = null;
    this.tiendaAbierta = false;
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

  /** Finaliza el turno, actualiza GameDTO y solo entonces recarga unidades. */
  terminarTurno(): void {
    if (!this.partida || this.modoDemo || this.accionEnCurso ||
      this.partida.status !== 'IN_PROGRESS') return;
    this.terminando = true;
    this.error = '';
    const dineroAntes: Record<number, number> = {};
    for (const jugador of this.partida.players) dineroAntes[jugador.turnOrder] = jugador.money;
    const turno = this.turnoActual();
    if (!turno) { this.terminando = false; return; }
    this.cdr.markForCheck();
    this.subs.add(this.api.finTurno(this.id, turno).subscribe({
      next: (actualizada) => {
        if (this.destruido) return;
        this.partida = actualizada;
        this.terminando = false;
        this.establecerUnidadActiva(null);
        this.fabricaActiva = null;
        if (actualizada.status === 'FINISHED') {
          void this.router.navigate(['/partidas', this.id, 'resultados']);
          return;
        }
        this.ganancias = {};
        for (const jugador of actualizada.players) {
          const diferencia = jugador.money - (dineroAntes[jugador.turnOrder] ?? jugador.money);
          if (diferencia > 0) this.ganancias[jugador.turnOrder] = diferencia;
        }
        this.mostrarAviso();
        this.recargarUnidades();
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        if (this.destruido) return;
        this.terminando = false;
        this.error = Juegocomponent.obtenerMensajeError(error, 'No se pudo terminar el turno.');
        this.cargarDatos();
        this.cdr.detectChanges();
      }
    }));
  }

  /** El backend recibe siempre la ronda y el orden del último GameDTO. */
  private turnoActual(): turnoactual | null {
    return this.partida ? {
      currentRound: this.partida.currentRound,
      currentPlayerOrder: this.partida.currentPlayerOrder
    } : null;
  }

  /** Permite capturar únicamente si la casilla tiene una propiedad ajena y la unidad es INFANTRY. */
  get puedeCapturar(): boolean {
    const unidad = this.unidadActiva;
    if (!unidad || unidad.unitType !== 'INFANTRY' || unidad.attackedThisTurn ||
      unidad.capturedThisTurn || this.partida?.status !== 'IN_PROGRESS') return false;
    return Boolean(this.tableroActual?.tiles.some(tile =>
      tile.x === unidad.x && tile.y === unidad.y &&
      tile.propertyType !== null && tile.ownerOrder !== unidad.ownerOrder));
  }

  /** Ejecuta captura en Spring Boot y consulta el nuevo estado del mapa. */
  capturarPropiedad(): void {
    const unidad = this.unidadActiva;
    const turno = this.turnoActual();
    if (!unidad || !turno || !this.puedeCapturar || this.accionEnCurso) return;
    this.capturando = true;
    this.error = '';
    this.subs.add(this.api.capturar(this.id, unidad.id, turno).subscribe({
      next: (resultado) => {
        if (this.destruido) return;
        this.capturando = false;
        this.establecerUnidadActiva(null);
        this.mensaje = resultado.captured
          ? 'Propiedad capturada. Control actualizado desde el servidor.'
          : `Captura en progreso: ${resultado.captureProgress}/${resultado.requiredProgress}.`;
        this.cargarMapa();
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        if (this.destruido) return;
        this.capturando = false;
        this.error = Juegocomponent.obtenerMensajeError(error, 'Captura rechazada.');
        this.cdr.detectChanges();
      }
    }));
  }

  /** Dinero del jugador que tiene el turno. */
  get dineroActual(): number {
    const actual = this.partida?.players.find((j) => j.turnOrder === this.partida?.currentPlayerOrder);
    return actual ? actual.money : 0;
  }

  /** Imagen de la unidad con el color del jugador en turno. */
  imagenTienda(tipo: string): string {
    const facciones = ['RED', 'BLUE', 'GREEN', 'YELLOW'];
    const faccion = facciones[(this.partida?.currentPlayerOrder ?? 1) - 1] ?? 'RED';
    return `/assets/sprites/unidades/${faccion}/${tipo}.png`;
  }

  /** Indica si hay una tropa encima de la fábrica elegida. */
  get fabricaOcupada(): boolean {
    const fabrica = this.fabricaActiva;
    if (!fabrica) return false;
    return this.unidades.some((u) => u.x === fabrica.x && u.y === fabrica.y && u.health > 0);
  }

  /** Abre la tienda en la primera fábrica propia que esté libre. */
  abrirTienda(): void {
    if (!this.partida || !this.tableroActual) return;
    const propias = this.tableroActual.tiles.filter((t) =>
      t.propertyType === 'FACTORY' && t.ownerOrder === this.partida?.currentPlayerOrder && t.propertyId != null);
    if (propias.length === 0) {
      this.error = 'No tienes fábricas. Captura una con tu infantería.';
      this.cdr.detectChanges();
      return;
    }
    const libre = propias.find((t) => !this.unidades.some((u) => u.x === t.x && u.y === t.y && u.health > 0));
    this.establecerUnidadActiva(null);
    this.fabricaActiva = libre ?? propias[0];
    this.tiendaAbierta = true;
    this.error = '';
    this.cdr.detectChanges();
  }

  /** Cierra la tienda. */
  cerrarTienda(): void {
    this.tiendaAbierta = false;
    this.fabricaActiva = null;
    this.cdr.detectChanges();
  }

  /** Compra una tropa en la fábrica elegida. El back valida el costo y el turno. */
  producirUnidad(tipo: 'INFANTRY' | 'TANK'): void {
    const turno = this.turnoActual();
    const propiedad = this.fabricaActiva;
    if (!turno || !propiedad?.propertyId || this.accionEnCurso ||
      this.partida?.status !== 'IN_PROGRESS') return;
    this.produciendo = true;
    this.error = '';
    this.subs.add(this.api.producir(this.id, propiedad.propertyId, tipo, turno).subscribe({
      next: (unidad) => {
        if (this.destruido) return;
        this.produciendo = false;
        this.fabricaActiva = null;
        this.tiendaAbierta = false;
        this.mensaje = `${this.nombreUnidad(unidad.unitType)} comprada. Podrá moverse en tu próximo turno.`;
        this.cargarDatos();
        this.cdr.detectChanges();
      },
      error: (error: unknown) => {
        if (this.destruido) return;
        this.produciendo = false;
        this.error = Juegocomponent.obtenerMensajeError(error, 'Producción rechazada.');
        this.cdr.detectChanges();
      }
    }));
  }

  /** Muestra "Turno de X" y lo que ganó durante 2 segundos. */
  private mostrarAviso(): void {
    this.avisoTurno = `TURNO DE ${this.jugadorEnTurno}`;
    setTimeout(() => {
      if (this.destruido) return;
      this.avisoTurno = '';
      this.ganancias = {};
      this.cdr.detectChanges();
    }, 2000);
  }

  /** Libera peticiones, suscripciones y recursos gráficos al salir. */

  ngOnDestroy(): void {
    this.destruido = true;
    this.suscripcionMovimientos?.unsubscribe();
    this.suscripcionRecargaUnidades?.unsubscribe();
    this.subs.unsubscribe();
    this.mapaPendiente = null;
    this.datosEscenaActuales = null;
    this.unidadActiva = null;
    this.tableroActual = null;
    this.movimientoEnCurso = false;
    this.ataqueEnCurso = false;
    this.recargandoUnidades = false;
    this.consultandoMovimientos = false;
    this.movimientosDisponibles = [];
    this.capturando = false;
    this.produciendo = false;
    if (this.phaser) {
      this.phaser.destroy(true);
      this.phaser = undefined;
    }
  }
}
