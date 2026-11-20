import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { usuario, AuthRequest, AuthResponse, historial, registro, respuestapendiente, verificacion, respuestalogin } from '../models/usuario';
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

  // Nombres con los que se guardan los datos de la sesión en el navegador.
  private claveToken = 'jwtToken';
  private claveRol = 'userRole';
  private claveUsuario = 'username';



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
    localStorage.setItem(this.claveToken, response.token);
    localStorage.setItem(this.claveRol, response.role);
    localStorage.setItem(this.claveUsuario, response.username);
  }

  /**
   * Obtiene el token de la sesión actual.
   * @returns el token, o null si no hay sesión
   */
  getToken(): string | null {
    return localStorage.getItem(this.claveToken);
  }

  /**
   * Obtiene el rol del usuario de la sesión actual.
   * @returns ADMIN o USER, o null si no hay sesión
   */
  getRole(): string | null {
    return localStorage.getItem(this.claveRol);
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
    localStorage.removeItem(this.claveToken);
    localStorage.removeItem(this.claveRol);
    localStorage.removeItem(this.claveUsuario);
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
   * @returns mensaje de respuesta del backend
   */
  deleteUsuario(id: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/user/deletebyid/${id}`, { headers: this.getHeaders(), responseType: 'text' });
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
  // ---------- REGISTRO Y VERIFICACIÓN DE CORREO ----------

  /**
   * Registra un usuario. El back le envía un código de 6 dígitos al correo.
   * @param datos usuario, correo y contraseña
   * @returns el challengeId que se necesita para verificar el código
   */
  registrar(datos: registro): Observable<respuestapendiente> {
    return this.http.post<respuestapendiente>(this.baseUrl + '/api/auth/register', datos);
  }

  /**
   * Verifica el código que llegó al correo y activa la cuenta.
   * @param datos challengeId y código
   * @returns mensaje del back
   */
  verificarCorreo(datos: verificacion): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(this.baseUrl + '/api/auth/verify-email', datos);
  }

  /**
   * Pide que se envíe un código nuevo al correo.
   * @param challengeId identificador del código anterior
   * @returns un challengeId nuevo
   */
  reenviarCodigo(challengeId: string): Observable<respuestapendiente> {
    return this.http.post<respuestapendiente>(this.baseUrl + '/api/auth/resend-otp', { challengeId });
  }
  // ---------- LOGIN CON CÓDIGO ----------

  /**
   * Revisa usuario y contraseña. Si están bien, el back envía un código al correo.
   * @param datos usuario y contraseña
   * @returns el challengeId que se necesita para verificar el código
   */
  iniciarSesion(datos: { username: string; password: string }): Observable<respuestapendiente> {
    return this.http.post<respuestapendiente>(this.baseUrl + '/api/auth/login', datos);
  }

  /**
   * Verifica el código del login. Si es correcto, el back entrega el token.
   * @param datos challengeId y código
   * @returns token y datos del usuario
   */
  verificarLogin(datos: verificacion): Observable<respuestalogin> {
    return this.http.post<respuestalogin>(this.baseUrl + '/api/auth/verify-otp', datos);
  }
}
