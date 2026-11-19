import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import * as Phaser from 'phaser';
import { juegoservice } from '../../services/juegoservice';
import { generadormapaservice } from '../../services/generadormapaservice';
import { EscenaMapa, DatosEscena, TAM_CASILLA } from '../../phaser/escenamapa';
import { casilla, configuracionmapa, terreno } from '../../models/mapa';

@Component({
  selector: 'app-juegocomponent',
  standalone: false,
  styleUrl: './juegocomponent.css',
  templateUrl: './juegocomponent.html',
})
/**
 * Pantalla del juego: genera un mapa aleatorio y lo dibuja con Phaser.
 */
export class Juegocomponent implements OnInit, OnDestroy {
  private juego = inject(juegoservice);
  private generador = inject(generadormapaservice);

  private juegoPhaser?: Phaser.Game;
  private config?: configuracionmapa;

  // Variables normales: cambian solo cuando el usuario hace clic o escribe.
  semilla = 12345;
  jugadores = 4;

  // Signals: cambian cuando llegan los datos o cuando Phaser avisa.
  terrenos = signal<terreno[]>([]);
  proporcion = signal('4 / 3');
  seleccion = signal<{ x: number; y: number; terreno?: terreno } | null>(null);

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
        this.terrenos.set(terrenos);
        this.config = config;
        this.crearJuego();
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
   * Crea el juego de Phaser y arranca la escena que dibuja el mapa.
   */
  private crearJuego() {
    const datos = this.armarDatos();
    this.proporcion.set(`${datos.mapa.width} / ${datos.mapa.height}`);

    this.juegoPhaser = new Phaser.Game({
      type: Phaser.AUTO,
      parent: 'contenedor-juego',
      width: datos.mapa.width * TAM_CASILLA,
      height: datos.mapa.height * TAM_CASILLA,
      backgroundColor: '#14201a',
      scale: { mode: Phaser.Scale.FIT, autoCenter: Phaser.Scale.NO_CENTER },
      scene: []
    });
    this.juegoPhaser.scene.add('EscenaMapa', EscenaMapa, true, datos);
  }

  /**
   * Genera el mapa con la semilla actual y junta todo lo que necesita la escena.
   * @returns los datos para la escena de Phaser
   */
  private armarDatos(): DatosEscena {
    const config = this.config as configuracionmapa;
    const mapa = this.generador.generar(config, this.semilla, this.jugadores);

    return {
      mapa,
      terrenos: this.terrenos(),
      bases: generadormapaservice.calcularPosicionesBase(mapa.width, mapa.height, this.jugadores),
      alPasarSobre: (c: casilla | null) => this.alPasarSobre(c)
    };
  }

  /**
   * Vuelve a dibujar el mapa con la semilla y los jugadores actuales.
   */
  private redibujar() {
    const escena = this.juegoPhaser?.scene.getScene('EscenaMapa');
    escena?.scene.restart(this.armarDatos());
    this.seleccion.set(null);
  }

  /**
   * Phaser llama a este método cada vez que el mouse pasa por otra casilla.
   * @param c casilla bajo el mouse, o null si el mouse salió del tablero
   */
  private alPasarSobre(c: casilla | null) {
    if (!c) {
      this.seleccion.set(null);
      return;
    }
    const terreno = this.terrenos().find(t => t.terrainType === c.terrainType);
    this.seleccion.set({ x: c.x, y: c.y, terreno });
  }

  /**
   * Genera un mapa nuevo con una semilla al azar.
   */
  nuevoMapa() {
    this.semilla = Math.floor(Math.random() * 1000000);
    this.redibujar();
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
    this.redibujar();
  }

  /**
   * Cambia la cantidad de jugadores y regenera el mapa.
   * @param n cantidad de jugadores (2 a 4)
   */
  fijarJugadores(n: number) {
    this.jugadores = n;
    this.redibujar();
  }

  /**
   * Texto con lo que cuesta a un tipo de movimiento pasar por un terreno.
   * @param t terreno
   * @param tipo código del tipo de movimiento
   * @returns el costo, o "No pasa" si es 0
   */
  costoDe(t: terreno, tipo: string): string {
    const costo = t.movementCost[tipo];
    return costo === 0 ? 'No pasa' : String(costo);
  }
}
