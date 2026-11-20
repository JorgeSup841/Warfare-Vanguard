import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { unidad } from '../models/unidad';
import { terreno, configuracionmapa } from '../models/mapa';
import { propiedad, faccion, comandante } from '../models/partida';

@Injectable({
  providedIn: 'root',
})
export class juegoservice {
  private baseUrl = 'assets/data';
  private http = inject(HttpClient);

  getUnidades(): Observable<unidad[]> {
    return this.http.get<unidad[]>(`${this.baseUrl}/Unidades.json`);
  }

  getTerrenos(): Observable<terreno[]> {
    return this.http.get<terreno[]>(`${this.baseUrl}/Terrenos.json`);
  }

  getPropiedades(): Observable<propiedad[]> {
    return this.http.get<propiedad[]>(`${this.baseUrl}/Propiedades.json`);
  }

  getFacciones(): Observable<faccion[]> {
    return this.http.get<faccion[]>(`${this.baseUrl}/Facciones.json`);
  }

  getComandantes(): Observable<comandante[]> {
    return this.http.get<comandante[]>(`${this.baseUrl}/Comandantes.json`);
  }

  getConfiguracionMapa(): Observable<configuracionmapa> {
    return this.http.get<configuracionmapa>(`${this.baseUrl}/Configuracionmapa.json`);
  }
}
