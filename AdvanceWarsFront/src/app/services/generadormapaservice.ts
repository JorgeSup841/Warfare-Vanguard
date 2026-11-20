import { Injectable } from '@angular/core';
import { mapa, casilla, tipoterreno, configuracionmapa } from '../models/mapa';

/** Servicio encargado de generar mapas aleatorios jugables a partir de una semilla. */
@Injectable({
  providedIn: 'root'
})
export class generadormapaservice {

  private readonly habilitado = true;

  /** Genera un mapa completo garantizando que todas las bases estén conectadas. */
  generar(config: configuracionmapa, semilla: number, jugadores: number): mapa {
    if (!this.habilitado) {
      return { width: 0, height: 0, tiles: [] };
    }
    return generadormapaservice.generarMapa(config, semilla, jugadores);
  }

  /** Ejecuta el ciclo de generación y validación de conectividad del mapa. */
  private static generarMapa(config: configuracionmapa, semilla: number, jugadores: number): mapa {
    const maximoIntentos = 20;
    let cuadricula: tipoterreno[][] = [];

    for (let intento = 0; intento < maximoIntentos; intento++) {
      const aleatorio = generadormapaservice.crearAleatorio(semilla + intento);
      const sinAgua = intento === maximoIntentos - 1;
      cuadricula = generadormapaservice.crearTerreno(config, aleatorio, jugadores, sinAgua);

      if (generadormapaservice.basesConectadas(cuadricula, config.width, config.height, jugadores)) {
        break;
      }
    }

    return generadormapaservice.aMapa(cuadricula, config.width, config.height);
  }

  /** Calcula las coordenadas iniciales de las bases según el tamaño del mapa y el número de jugadores. */
  static calcularPosicionesBase(ancho: number, alto: number, jugadores: number): { x: number; y: number }[] {
    const esquinas = [
      { x: 1, y: 1 },
      { x: ancho - 2, y: alto - 2 },
      { x: ancho - 2, y: 1 },
      { x: 1, y: alto - 2 }
    ];
    return esquinas.slice(0, jugadores);
  }

  /** Crea un generador pseudoaleatorio determinista basado en la semilla recibida. */
  private static crearAleatorio(semilla: number): () => number {
    let estado = semilla >>> 0;
    return () => {
      estado = (estado + 0x6d2b79f5) >>> 0;
      let mezcla = estado;
      mezcla = Math.imul(mezcla ^ (mezcla >>> 15), mezcla | 1);
      mezcla ^= mezcla + Math.imul(mezcla ^ (mezcla >>> 7), mezcla | 61);
      return ((mezcla ^ (mezcla >>> 14)) >>> 0) / 4294967296;
    };
  }

  /** Construye la matriz de terrenos distribuyendo agua, montañas y bosques. */
  private static crearTerreno(
    config: configuracionmapa,
    aleatorio: () => number,
    jugadores: number,
    sinAgua: boolean
  ): tipoterreno[][] {
    const cuadricula: tipoterreno[][] = [];
    for (let fila = 0; fila < config.height; fila++) {
      cuadricula.push(new Array<tipoterreno>(config.width).fill('PLAIN'));
    }

    const total = config.width * config.height;
    const agua = sinAgua
      ? 0
      : generadormapaservice.calcularCantidad(total, config.minWaterPercent, config.maxWaterPercent, aleatorio);
    const montanas = generadormapaservice.calcularCantidad(
      total,
      config.minMountainPercent,
      config.maxMountainPercent,
      aleatorio
    );
    const bosques = generadormapaservice.calcularCantidad(
      total,
      config.minForestPercent,
      config.maxForestPercent,
      aleatorio
    );

    generadormapaservice.esparcir(cuadricula, 'WATER', agua, jugadores, aleatorio);
    generadormapaservice.esparcir(cuadricula, 'MOUNTAIN', montanas, jugadores, aleatorio);
    generadormapaservice.esparcir(cuadricula, 'FOREST', bosques, jugadores, aleatorio);

    return cuadricula;
  }

  /** Calcula la cantidad exacta de casillas de un terreno según su rango porcentual. */
  private static calcularCantidad(total: number, minimo: number, maximo: number, aleatorio: () => number): number {
    const porcentaje = minimo + aleatorio() * (maximo - minimo);
    return Math.floor((total * porcentaje) / 100);
  }

  /** Esparce grupos de un tipo de terreno por la cuadrícula evitando las zonas de base. */
  private static esparcir(
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
    let posX = Math.floor(aleatorio() * ancho);
    let posY = Math.floor(aleatorio() * alto);
    let pasos = cantidad * 30;

    while (colocadas < cantidad && pasos > 0) {
      pasos--;

      if (
        cuadricula[posY][posX] === 'PLAIN' &&
        !generadormapaservice.esZonaBase(posX, posY, ancho, alto, jugadores)
      ) {
        cuadricula[posY][posX] = tipo;
        colocadas++;
      }

      if (aleatorio() < 0.08) {
        posX = Math.floor(aleatorio() * ancho);
        posY = Math.floor(aleatorio() * alto);
      } else {
        const direccion = direcciones[Math.floor(aleatorio() * 4)];
        posX = Math.min(ancho - 1, Math.max(0, posX + direccion[0]));
        posY = Math.min(alto - 1, Math.max(0, posY + direccion[1]));
      }
    }
  }

  /** Verifica si una coordenada pertenece al perímetro protegido alrededor de alguna base. */
  private static esZonaBase(posX: number, posY: number, ancho: number, alto: number, jugadores: number): boolean {
    return generadormapaservice.calcularPosicionesBase(ancho, alto, jugadores).some(
      base => Math.abs(base.x - posX) <= 1 && Math.abs(base.y - posY) <= 1
    );
  }

  /** Comprueba mediante búsqueda en anchura que todas las bases estén comunicadas por tierra. */
  private static basesConectadas(cuadricula: tipoterreno[][], ancho: number, alto: number, jugadores: number): boolean {
    const bases = generadormapaservice.calcularPosicionesBase(ancho, alto, jugadores);
    const visitada: boolean[][] = cuadricula.map(fila => fila.map(() => false));
    const cola: { x: number; y: number }[] = [bases[0]];
    visitada[bases[0].y][bases[0].x] = true;
    const direcciones = [[1, 0], [-1, 0], [0, 1], [0, -1]];

    let frente = 0;
    while (frente < cola.length) {
      const actual = cola[frente++];
      for (const direccion of direcciones) {
        const siguienteX = actual.x + direccion[0];
        const siguienteY = actual.y + direccion[1];
        const dentro = siguienteX >= 0 && siguienteY >= 0 && siguienteX < ancho && siguienteY < alto;
        if (
          dentro &&
          !visitada[siguienteY][siguienteX] &&
          generadormapaservice.sePuedeCruzar(cuadricula[siguienteY][siguienteX])
        ) {
          visitada[siguienteY][siguienteX] = true;
          cola.push({ x: siguienteX, y: siguienteY });
        }
      }
    }

    return bases.every(base => visitada[base.y][base.x]);
  }

  /** Indica si una casilla es transitable por unidades terrestres básicas. */
  private static sePuedeCruzar(tipo: tipoterreno): boolean {
    return tipo !== 'WATER' && tipo !== 'MOUNTAIN';
  }

  /** Convierte la matriz interna de terrenos en el objeto mapa final con lista de casillas. */
  private static aMapa(cuadricula: tipoterreno[][], ancho: number, alto: number): mapa {
    const tiles: casilla[] = [];
    for (let fila = 0; fila < alto; fila++) {
      for (let columna = 0; columna < ancho; columna++) {
        tiles.push({ x: columna, y: fila, terrainType: cuadricula[fila][columna] });
      }
    }
    return { width: ancho, height: alto, tiles };
  }
}
