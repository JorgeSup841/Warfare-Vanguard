import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { unidad } from '../models/unidad';
import { terreno, configuracionmapa } from '../models/mapa';
import { propiedad, faccion, comandante } from '../models/partida';

/**
 * Servicio con los datos fijos del juego: unidades, terrenos, propiedades,
 * facciones, comandantes y configuración del mapa.
 * Por ahora lee los JSON de public/assets/data. Cuando el backend esté listo
 * se cambia baseUrl por 'http://localhost:8080'.
 */
@Injectable({
  providedIn: 'root'
})
export class juegoservice {
  private baseUrl = 'assets/data';

  private http = inject(HttpClient);



  /**
   * Trae las fichas de las unidades con sus armas y estadísticas.
   * @returns lista de unidades
   */
  getUnidades(): Observable<unidad[]> {
    return this.http.get<unidad[]>(`${this.baseUrl}/Unidades.json`);
  }

  /**
   * Trae los tipos de terreno con su costo de movimiento y su defensa.
   * @returns lista de terrenos
   */
  getTerrenos(): Observable<terreno[]> {
    return this.http.get<terreno[]>(`${this.baseUrl}/Terrenos.json`);
  }

  /**
   * Trae los tipos de propiedad (ciudad, fábrica, aeropuerto, puerto, cuartel general).
   * @returns lista de propiedades
   */
  getPropiedades(): Observable<propiedad[]> {
    return this.http.get<propiedad[]>(`${this.baseUrl}/Propiedades.json`);
  }

  /**
   * Trae las facciones (ejércitos) que puede elegir un jugador.
   * @returns lista de facciones
   */
  getFacciones(): Observable<faccion[]> {
    return this.http.get<faccion[]>(`${this.baseUrl}/Facciones.json`);
  }

  /**
   * Trae los comandantes de cada facción con sus habilidades.
   * @returns lista de comandantes
   */
  getComandantes(): Observable<comandante[]> {
    return this.http.get<comandante[]>(`${this.baseUrl}/Comandantes.json`);
  }

  /**
   * Trae la configuración de generación de mapas que maneja el administrador.
   * @returns mínimos y máximos de agua, bosque, montaña y tamaño del mapa
   */
  getConfiguracionMapa(): Observable<configuracionmapa> {
    return this.http.get<configuracionmapa>(`${this.baseUrl}/Configuracionmapa.json`);
  }
}
