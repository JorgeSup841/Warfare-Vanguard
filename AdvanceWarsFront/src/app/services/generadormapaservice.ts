
import { Injectable } from '@angular/core';
import { mapa, casilla, tipoterreno, configuracionmapa } from '../models/mapa';

@Injectable({
  providedIn: 'root'
})
export class generadormapaservice {

  generar(config: configuracionmapa, semilla: number, jugadores: number): mapa {
    const maximoIntentos = 20;
    let cuadricula: tipoterreno[][] = [];

    for (let intento = 0; intento < maximoIntentos; intento++) {
      const aleatorio = this.crearAleatorio(semilla + intento);
      const sinAgua = intento === maximoIntentos - 1;
      cuadricula = this.crearTerreno(config, aleatorio, jugadores, sinAgua);

      if (this.basesConectadas(cuadricula, config.width, config.height, jugadores)) {
        break;
      }
    }

    return this.aMapa(cuadricula, config.width, config.height);
  }

  posicionesBase(ancho: number, alto: number, jugadores: number): { x: number; y: number }[] {
    const esquinas = [
      { x: 1, y: 1 },
      { x: ancho - 2, y: alto - 2 },
      { x: ancho - 2, y: 1 },
      { x: 1, y: alto - 2 }
    ];
    return esquinas.slice(0, jugadores);
  }

  private crearAleatorio(semilla: number): () => number {
    let estado = semilla >>> 0;
    return () => {
      estado = (estado + 0x6d2b79f5) >>> 0;
      let t = estado;
      t = Math.imul(t ^ (t >>> 15), t | 1);
      t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }

  private crearTerreno(
    config: configuracionmapa,
    aleatorio: () => number,
    jugadores: number,
    sinAgua: boolean
  ): tipoterreno[][] {
    const cuadricula: tipoterreno[][] = [];
    for (let y = 0; y < config.height; y++) {
      cuadricula.push(new Array<tipoterreno>(config.width).fill('PLAIN'));
    }

    const total = config.width * config.height;
    const agua = sinAgua ? 0 : this.cantidad(total, config.minWaterPercent, config.maxWaterPercent, aleatorio);
    const montanas = this.cantidad(total, config.minMountainPercent, config.maxMountainPercent, aleatorio);
    const bosques = this.cantidad(total, config.minForestPercent, config.maxForestPercent, aleatorio);

    this.esparcir(cuadricula, 'WATER', agua, jugadores, aleatorio);
    this.esparcir(cuadricula, 'MOUNTAIN', montanas, jugadores, aleatorio);
    this.esparcir(cuadricula, 'FOREST', bosques, jugadores, aleatorio);

    return cuadricula;
  }

  private cantidad(total: number, minimo: number, maximo: number, aleatorio: () => number): number {
    const porcentaje = minimo + aleatorio() * (maximo - minimo);
    return Math.floor((total * porcentaje) / 100);
  }

  private esparcir(
    cuadricula: tipoterreno[][],
    tipo: tipoterreno,
    cantidad: number,
    jugadores: number,
    aleatorio: () => number
  ): void {
    const alto = cuadricula.length;
    const ancho = cuadricula[0].length;
    const direcciones = [[1, 0], [-1, 0], [0, 1], [0, -1]];

    let colocadas = 0;
    let x = Math.floor(aleatorio() * ancho);
    let y = Math.floor(aleatorio() * alto);
    let pasos = cantidad * 30;

    while (colocadas < cantidad && pasos > 0) {
      pasos--;

      if (cuadricula[y][x] === 'PLAIN' && !this.esZonaBase(x, y, ancho, alto, jugadores)) {
        cuadricula[y][x] = tipo;
        colocadas++;
      }

      if (aleatorio() < 0.08) {
        x = Math.floor(aleatorio() * ancho);
        y = Math.floor(aleatorio() * alto);
      } else {
        const d = direcciones[Math.floor(aleatorio() * 4)];
        x = Math.min(ancho - 1, Math.max(0, x + d[0]));
        y = Math.min(alto - 1, Math.max(0, y + d[1]));
      }
    }
  }

  private esZonaBase(x: number, y: number, ancho: number, alto: number, jugadores: number): boolean {
    return this.posicionesBase(ancho, alto, jugadores).some(
      b => Math.abs(b.x - x) <= 1 && Math.abs(b.y - y) <= 1
    );
  }

  private basesConectadas(cuadricula: tipoterreno[][], ancho: number, alto: number, jugadores: number): boolean {
    const bases = this.posicionesBase(ancho, alto, jugadores);
    const visitada: boolean[][] = cuadricula.map(fila => fila.map(() => false));
    const cola: { x: number; y: number }[] = [bases[0]];
    visitada[bases[0].y][bases[0].x] = true;
    const direcciones = [[1, 0], [-1, 0], [0, 1], [0, -1]];

    let frente = 0;
    while (frente < cola.length) {
      const actual = cola[frente++];
      for (const d of direcciones) {
        const nx = actual.x + d[0];
        const ny = actual.y + d[1];
        const dentro = nx >= 0 && ny >= 0 && nx < ancho && ny < alto;
        if (dentro && !visitada[ny][nx] && this.sePuedeCruzar(cuadricula[ny][nx])) {
          visitada[ny][nx] = true;
          cola.push({ x: nx, y: ny });
        }
      }
    }

    return bases.every(b => visitada[b.y][b.x]);
  }

  private sePuedeCruzar(tipo: tipoterreno): boolean {
    return tipo !== 'WATER' && tipo !== 'MOUNTAIN';
  }

  private aMapa(cuadricula: tipoterreno[][], ancho: number, alto: number): mapa {
    const tiles: casilla[] = [];
    for (let y = 0; y < alto; y++) {
      for (let x = 0; x < ancho; x++) {
        tiles.push({ x, y, terrainType: cuadricula[y][x] });
      }
    }
    return { width: ancho, height: alto, tiles };
  }
}
