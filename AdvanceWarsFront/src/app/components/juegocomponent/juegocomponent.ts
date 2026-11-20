import { Component, OnDestroy, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import Phaser from 'phaser';
import { juegoservice } from '../../services/juegoservice';
import { tableroservice } from '../../services/tableroservice';
import { EscenaMapa, DatosEscena, TAM_CASILLA } from '../../phaser/escenamapa';
import { casilla, configuracionmapa, terreno, mapa, tipoterreno } from '../../models/mapa';
import { tablero } from '../../models/tablero';

@Component({
  selector: 'app-juegocomponent',
  standalone: false,
  styleUrl: './juegocomponent.css',
  templateUrl: './juegocomponent.html',
})
/**
 * Pantalla del juego: le pide al back un mapa aleatorio y lo dibuja con Phaser.
 */
export class Juegocomponent implements OnInit, OnDestroy {
  private juego = inject(juegoservice);
  private tableroService = inject(tableroservice);
  private cd = inject(ChangeDetectorRef);

  private juegoPhaser?: Phaser.Game;
  private config?: configuracionmapa;
  readonly obtenerCosto = Juegocomponent.costoDe;

  semilla = 12345;
  jugadores = 4;
  terrenos: terreno[] = [];
  proporcion = '4 / 3';
  mensaje = '';

  // Casilla que está bajo el mouse (null si el mouse está fuera del mapa).
  seleccion: { x: number; y: number; terreno?: terreno } | null = null;

  // Tipos de movimiento para mostrar cuánto le cuesta a cada uno pasar por un terreno.
  tiposMovimiento = [
    { codigo: 'FOOT', nombre: 'Infantería' },
    { codigo: 'MECH', nombre: 'Mecanizada' },
    { codigo: 'TREAD', nombre: 'Oruga' },
    { codigo: 'WHEEL', nombre: 'Ruedas' },
    { codigo: 'AIR', nombre: 'Aire' },
    { codigo: 'SHIP', nombre: 'Barco' }
  ];

  /**
   * Al abrir la pantalla carga los terrenos y la configuración, y cuando llegan crea el juego.
   */
  ngOnInit() {
    this.juego.getTerrenos().subscribe(terrenos => {
      this.juego.getConfiguracionMapa().subscribe(config => {
        this.terrenos = terrenos;
        this.config = config;
        this.pedirMapa();
      });
    });
  }

  /**
   * Cuando se sale de la pantalla se destruye el juego de Phaser para liberar memoria.
   */
  ngOnDestroy() {
    this.juegoPhaser?.destroy(true);
  }

  /**
   * Le pide al back una partida con la semilla y los jugadores actuales,
   * trae su mapa y lo dibuja.
   */
  private pedirMapa() {
    const config = this.config as configuracionmapa;

    const nombres = [];
    for (let i = 1; i <= this.jugadores; i++) {
      nombres.push('Jugador ' + i);
    }
    const datos = { playerNames: nombres, width: config.width, height: config.height, seed: this.semilla };

    this.mensaje = '';
    this.tableroService.crearPartida(datos).subscribe({
      next: partida => {
        this.tableroService.getTablero(partida.id).subscribe(t => this.dibujar(t));
      },
      error: () => {
        this.mensaje = 'No se pudo generar el mapa. Inicia sesión y revisa que el back esté encendido.';
        this.cd.detectChanges();
      }
    });
  }

  /**
   * Convierte el mapa del back al formato que usa la escena y lo dibuja.
   * La primera vez crea el juego de Phaser; después solo reinicia la escena.
   * @param t mapa que llegó del back
   */
  private dibujar(t: tablero) {
    // El back llama "terrain" a lo que la escena llama "terrainType".
    const mapaEscena: mapa = {
      width: t.width,
      height: t.height,
      tiles: t.tiles.map(c => ({ x: c.x, y: c.y, terrainType: c.terrain as tipoterreno }))
    };

    // Las bases son las casillas con cuartel general, ordenadas por jugador.
    const bases = t.tiles
      .filter(c => c.propertyType === 'HEADQUARTERS')
      .sort((a, b) => (a.ownerOrder ?? 0) - (b.ownerOrder ?? 0))
      .map(c => ({ x: c.x, y: c.y }));

    const datos: DatosEscena = {
      mapa: mapaEscena,
      terrenos: this.terrenos,
      bases,
      alPasarSobre: (c: casilla | null) => this.alPasarSobre(c)
    };

    this.proporcion = t.width + ' / ' + t.height;
    this.seleccion = null;
    this.cd.detectChanges();

    if (!this.juegoPhaser) {
      this.juegoPhaser = new Phaser.Game({
        type: Phaser.AUTO,
        parent: 'contenedor-juego',
        width: t.width * TAM_CASILLA,
        height: t.height * TAM_CASILLA,
        backgroundColor: '#14201a',
        scale: { mode: Phaser.Scale.FIT, autoCenter: Phaser.Scale.NO_CENTER },
        scene: []
      });
      this.juegoPhaser.scene.add('EscenaMapa', EscenaMapa, true, datos);
    } else {
      this.juegoPhaser.scene.getScene('EscenaMapa')?.scene.restart(datos);
    }
  }

  /**
   * Phaser llama a este método cada vez que el mouse pasa por otra casilla.
   * @param c casilla bajo el mouse, o null si el mouse salió del tablero
   */
  private alPasarSobre(c: casilla | null) {
    if (!c) {
      this.seleccion = null;
    } else {
      const terreno = this.terrenos.find(t => t.terrainType === c.terrainType);
      this.seleccion = { x: c.x, y: c.y, terreno };
    }
    this.cd.detectChanges();
  }

  /**
   * Genera un mapa nuevo con una semilla al azar.
   */
  nuevoMapa() {
    this.semilla = Math.floor(Math.random() * 1000000);
    this.pedirMapa();
  }

  /**
   * Genera el mapa con la semilla que escribió el usuario en la caja.
   */
  usarSemilla() {
    const numero = Number(this.semilla);
    if (isNaN(numero)) {
      return;
    }
    this.semilla = Math.floor(numero);
    this.pedirMapa();
  }

  /**
   * Cambia la cantidad de jugadores y regenera el mapa.
   * @param n cantidad de jugadores (2 a 4)
   */
  fijarJugadores(n: number) {
    this.jugadores = n;
    this.pedirMapa();
  }

  /**
   * Texto con lo que cuesta a un tipo de movimiento pasar por un terreno.
   * @param t terreno
   * @param tipo código del tipo de movimiento
   * @returns el costo, o "No pasa" si es 0
   */
  static costoDe(t: terreno, tipo: string): string {
    const costo = t.movementCost[tipo];
    return costo === 0 ? 'No pasa' : String(costo);
  }
}
