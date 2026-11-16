import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { unidad } from '../models/unidad';
import { terreno, configuracionmapa } from '../models/mapa';
import { propiedad, faccion, comandante } from '../models/partida';

@Injectable({
  providedIn: 'root'
})
export class juegoservice {
  // Por ahora lee los JSON de public/assets/data.
  // Cuando el backend esté listo se cambia por: 'http://localhost:8080'
  private baseUrl = 'assets/data';

  private http = inject(HttpClient);



  getUnidades(): Observable<unidad[]> {
    return this.http.get<unidad[]>(`${this.baseUrl}/unidades.json`);
  }

  getTerrenos(): Observable<terreno[]> {
    return this.http.get<terreno[]>(`${this.baseUrl}/terrenos.json`);
  }

  getPropiedades(): Observable<propiedad[]> {
    return this.http.get<propiedad[]>(`${this.baseUrl}/propiedades.json`);
  }

  getFacciones(): Observable<faccion[]> {
    return this.http.get<faccion[]>(`${this.baseUrl}/facciones.json`);
  }

  getComandantes(): Observable<comandante[]> {
    return this.http.get<comandante[]>(`${this.baseUrl}/comandantes.json`);
  }

  getConfiguracionMapa(): Observable<configuracionmapa> {
    return this.http.get<configuracionmapa>(`${this.baseUrl}/configuracionmapa.json`);
  }
}
