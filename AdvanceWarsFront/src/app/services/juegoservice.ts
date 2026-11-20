import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { unidad } from '../models/unidad';
import { terreno, configuracionmapa } from '../models/mapa';
import { propiedad, faccion, comandante } from '../models/partida';

/** Accede a los catálogos JSON locales que utiliza el tablero táctico. */
@Injectable({
  providedIn: 'root',
})
export class juegoservice {
  private baseUrl = 'assets/data';
  private http = inject(HttpClient);
  /** Obtiene las definiciones estáticas de unidades de juego. */
  getUnidades(): Observable<unidad[]> {
    return this.http.get<unidad[]>(`${this.baseUrl}/Unidades.json`);
  }

  /** Carga el catálogo local de tipos de terreno. */
  getTerrenos(): Observable<terreno[]> {
    return this.http.get<terreno[]>(`${this.baseUrl}/Terrenos.json`);
  }

  /** Carga las definiciones de propiedades. */
  getPropiedades(): Observable<propiedad[]> {
    return this.http.get<propiedad[]>(`${this.baseUrl}/Propiedades.json`);
  }

  /** Carga las facciones disponibles. */
  getFacciones(): Observable<faccion[]> {
    return this.http.get<faccion[]>(`${this.baseUrl}/Facciones.json`);
  }

  /** Carga las definiciones de comandantes. */
  getComandantes(): Observable<comandante[]> {
    return this.http.get<comandante[]>(`${this.baseUrl}/Comandantes.json`);
  }

  /** Obtiene las dimensiones y reglas de configuración del mapa. */
  getConfiguracionMapa(): Observable<configuracionmapa> {
    return this.http.get<configuracionmapa>(`${this.baseUrl}/Configuracionmapa.json`);
  }
}
