import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';
import { usuarioservice } from './usuarioservice';
import { usuario, rolusuario, registro, respuestapendiente } from '../models/usuario';
import {
  resumenadmin, partidaadmin, mapaadmin, tropaadmin,
  registrobatallaadmin, sesionadmin, actividadadmin
} from '../models/admin';

/** Operaciones administrativas respaldadas por /api/admin de Spring Boot. */
@Injectable({ providedIn: 'root' })
export class adminservice {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(usuarioservice);
  private readonly api = `${API_BASE_URL}/admin`;

  /** Consulta indicadores del panel. */
  resumen(): Observable<resumenadmin> {
    return this.http.get<resumenadmin>(`${this.api}/dashboard`, { headers: this.auth.getHeaders() });
  }

  /** Consulta todas las cuentas como ADMIN. */
  listarUsuarios(): Observable<usuario[]> {
    return this.http.get<usuario[]>(`${this.api}/users`, { headers: this.auth.getHeaders() });
  }

  /** Consulta una cuenta por identificador. */
  obtenerUsuario(id: number): Observable<usuario> {
    return this.http.get<usuario>(`${this.api}/users/${id}`, { headers: this.auth.getHeaders() });
  }

  /** Registra una cuenta USER, que deberá validar su correo mediante OTP. */
  crearUsuario(datos: registro): Observable<respuestapendiente> {
    return this.http.post<respuestapendiente>(`${this.api}/users`, datos,
      { headers: this.auth.getHeaders() });
  }

  /** Cambia el rol de otra cuenta. */
  cambiarRol(id: number, role: rolusuario): Observable<usuario> {
    return this.http.patch<usuario>(`${this.api}/users/${id}/role`, { role },
      { headers: this.auth.getHeaders() });
  }

  /** Cambia el estado de otra cuenta verificada. */
  cambiarEstado(id: number, enabled: boolean): Observable<usuario> {
    return this.http.patch<usuario>(`${this.api}/users/${id}/enabled`, { enabled },
      { headers: this.auth.getHeaders() });
  }

  /** Lista todas las partidas, incluidas las de otros usuarios. */
  listarPartidas(): Observable<partidaadmin[]> {
    return this.http.get<partidaadmin[]>(`${this.api}/games`, { headers: this.auth.getHeaders() });
  }

  /** Consulta el resumen administrativo de una partida. */
  obtenerPartida(id: number): Observable<partidaadmin> {
    return this.http.get<partidaadmin>(`${this.api}/games/${id}`, { headers: this.auth.getHeaders() });
  }

  /** Consulta las casillas de una partida. */
  obtenerMapa(id: number): Observable<mapaadmin> {
    return this.http.get<mapaadmin>(`${this.api}/games/${id}/map`, { headers: this.auth.getHeaders() });
  }

  /** Consulta las tropas de una partida. */
  obtenerTropas(id: number): Observable<tropaadmin[]> {
    return this.http.get<tropaadmin[]>(`${this.api}/games/${id}/units`, { headers: this.auth.getHeaders() });
  }

  /** Consulta el historial de eventos de una partida. */
  obtenerHistorial(id: number): Observable<registrobatallaadmin[]> {
    return this.http.get<registrobatallaadmin[]>(`${this.api}/games/${id}/history`,
      { headers: this.auth.getHeaders() });
  }

  /** Elimina una partida, acción irreversible. */
  eliminarPartida(id: number): Observable<null> {
    return this.http.delete<null>(`${this.api}/games/${id}`, { headers: this.auth.getHeaders() });
  }

  /** Consulta todas las sesiones (incluidas las terminadas). */
  listarSesiones(): Observable<sesionadmin[]> {
    return this.http.get<sesionadmin[]>(`${this.api}/sessions`, { headers: this.auth.getHeaders() });
  }

  /** Consulta únicamente las sesiones activas. */
  listarSesionesActivas(): Observable<sesionadmin[]> {
    return this.http.get<sesionadmin[]>(`${this.api}/sessions/active`,
      { headers: this.auth.getHeaders() });
  }

  /** Revoca una sesión concreta por su identificador. */
  revocarSesion(id: string): Observable<null> {
    return this.http.delete<null>(`${this.api}/sessions/${encodeURIComponent(id)}`,
      { headers: this.auth.getHeaders() });
  }

  /** Consulta hasta 200 eventos recientes de actividad. */
  listarActividad(): Observable<actividadadmin[]> {
    return this.http.get<actividadadmin[]>(`${this.api}/activity`, { headers: this.auth.getHeaders() });
  }

  /** Consulta la actividad de una cuenta concreta. */
  actividadUsuario(id: number): Observable<actividadadmin[]> {
    return this.http.get<actividadadmin[]>(`${this.api}/users/${id}/activity`,
      { headers: this.auth.getHeaders() });
  }
}
