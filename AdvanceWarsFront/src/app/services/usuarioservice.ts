import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { usuario, AuthRequest, AuthResponse, historial } from '../models/usuario';

/**
 * Servicio de usuarios: inicio de sesión, registro, manejo de la sesión,
 * administración de usuarios e historial de partidas. Se comunica con el backend.
 */
@Injectable({
  providedIn: 'root'
})
export class usuarioservice {
  private baseUrl = 'http://localhost:8080';

  private http = inject(HttpClient);



  /**
   * Inicia sesión en el backend.
   * @param credentials usuario y contraseña
   * @returns token, rol y nombre del usuario
   */
  login(credentials: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, credentials);
  }

  /**
   * Registra un usuario nuevo con rol USER.
   * @param usuario datos del usuario a registrar
   * @returns mensaje de respuesta del backend
   */
  register(usuario: usuario): Observable<string> {
    const nuevoUsuario = { ...usuario, role: 'USER' as const };
    return this.http.post(`${this.baseUrl}/auth/register`, nuevoUsuario, { responseType: 'text' });
  }



  /**
   * Guarda en el navegador el token, el rol y el nombre del usuario que inició sesión.
   * @param response respuesta del login
   */
  saveSession(response: AuthResponse): void {
    localStorage.setItem('jwtToken', response.token);
    localStorage.setItem('userRole', response.role);
    localStorage.setItem('username', response.username);
  }

  /**
   * Obtiene el token de la sesión actual.
   * @returns el token, o null si no hay sesión
   */
  getToken(): string | null {
    return localStorage.getItem('jwtToken');
  }

  /**
   * Obtiene el rol del usuario de la sesión actual.
   * @returns ADMIN o USER, o null si no hay sesión
   */
  getRole(): string | null {
    return localStorage.getItem('userRole');
  }

  /**
   * Indica si el usuario de la sesión actual es administrador.
   * @returns true si el rol es ADMIN
   */
  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  /**
   * Cierra la sesión borrando los datos guardados en el navegador.
   */
  logout(): void {
    localStorage.clear();
  }

  /**
   * Arma los encabezados de las peticiones con el token de la sesión.
   * @returns encabezados con Content-Type y Authorization
   */
  private getHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }



  /**
   * Trae todos los usuarios registrados (solo administrador).
   * @returns lista de usuarios
   */
  getUsuarios(): Observable<usuario[]> {
    return this.http.get<usuario[]>(`${this.baseUrl}/user/getall`, { headers: this.getHeaders() });
  }

  /**
   * Busca un usuario por su id.
   * @param id id del usuario
   * @returns el usuario encontrado
   */
  getUsuarioById(id: number): Observable<usuario> {
    return this.http.get<usuario>(`${this.baseUrl}/user/getbyid/${id}`, { headers: this.getHeaders() });
  }

  /**
   * Actualiza los datos de un usuario.
   * @param id id del usuario a actualizar
   * @param usuario datos nuevos
   * @returns el usuario actualizado
   */
  updateUsuario(id: number, usuario: usuario): Observable<usuario> {
    return this.http.put<usuario>(`${this.baseUrl}/user/updatejson?id=${id}`, usuario, { headers: this.getHeaders() });
  }

  /**
   * Elimina un usuario.
   * @param id id del usuario a eliminar
   */
  deleteUsuario(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/user/deletebyid/${id}`, { headers: this.getHeaders() });
  }



  /**
   * Guarda el resultado de una partida terminada para el usuario de la sesión.
   * @param resultado resumen de la partida (rondas, bajas, si ganó)
   * @returns la partida guardada
   */
  guardarPartida(resultado: historial): Observable<historial> {
    return this.http.post<historial>(`${this.baseUrl}/historial/save`, resultado, { headers: this.getHeaders() });
  }

  /**
   * Trae el historial de partidas del usuario de la sesión.
   * @returns lista de partidas jugadas
   */
  getHistorial(): Observable<historial[]> {
    return this.http.get<historial[]>(`${this.baseUrl}/historial/mine`, { headers: this.getHeaders() });
  }
}
