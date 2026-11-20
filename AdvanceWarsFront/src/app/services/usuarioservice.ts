import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { API_BASE_URL } from './api-config';
import {
  registro,
  respuestapendiente,
  verificacion,
  respuestalogin,
  desafioautenticacion,
  usuario,
  usuarioautenticado,
  rolusuario,
  respuestaMensaje,
} from '../models/usuario';

@Injectable({ providedIn: 'root' })
export class usuarioservice {
  private readonly api = `${API_BASE_URL}/auth`;
  private readonly usuariosApi = `${API_BASE_URL}/users`;
  private token: string | null = null;
  private usuarioActual: usuarioautenticado | null = null;
  private expira: number | null = null;
  private desafio: desafioautenticacion | null = null;
  private recuperacionId: string | null = null;
  private sesionSubject = new BehaviorSubject<usuarioautenticado | null>(null);
  readonly sesion$ = this.sesionSubject.asObservable();

  constructor(private http: HttpClient) {}

  registrar(datos: registro): Observable<respuestapendiente> {
    return this.http.post<respuestapendiente>(`${this.api}/register`, datos);
  }
  iniciarSesion(datos: { username: string; password: string }): Observable<respuestapendiente> {
    return this.http.post<respuestapendiente>(`${this.api}/login`, datos);
  }
  // El usuario solo introduce el código. challengeId permanece en memoria del servicio.
  verificarCorreo(codigo: string): Observable<respuestaMensaje> {
    return this.http.post<respuestaMensaje>(
      `${this.api}/verify-email`,
      this.datosCodigo(codigo, 'correo'),
    );
  }
  verificarLogin(codigo: string): Observable<respuestalogin> {
    return this.http
      .post<respuestalogin>(`${this.api}/verify-otp`, this.datosCodigo(codigo, 'login'))
      .pipe(tap((respuesta) => this.guardarSesion(respuesta)));
  }
  reenviarCodigo(): Observable<respuestapendiente> {
    if (!this.desafio) throw new Error('No existe una verificación pendiente.');
    return this.http
      .post<respuestapendiente>(`${this.api}/resend-otp`, {
        challengeId: this.desafio.challengeId,
      })
      .pipe(
        tap((respuesta) => {
          if (this.desafio)
            this.desafio = {
              ...this.desafio,
              challengeId: respuesta.challengeId,
              expiresAt: respuesta.expiresAt,
            };
        }),
      );
  }
  prepararDesafio(tipo: 'correo' | 'login', respuesta: respuestapendiente, correo?: string): void {
    if (!respuesta?.challengeId) throw new Error('El backend no devolvió challengeId.');
    this.desafio = {
      tipo,
      challengeId: respuesta.challengeId,
      correo,
      expiresAt: respuesta.expiresAt,
    };
  }
  getDesafio(): desafioautenticacion | null {
    return this.desafio;
  }
  limpiarDesafio(): void {
    this.desafio = null;
  }
  private datosCodigo(codigo: string, tipo: 'correo' | 'login'): verificacion {
    if (!this.desafio || this.desafio.tipo !== tipo)
      throw new Error('Inicia otra vez el registro o el login para recibir un código.');
    return { challengeId: this.desafio.challengeId, code: codigo.trim() };
  }
  private guardarSesion(respuesta: respuestalogin): void {
    if (!respuesta.accessToken || !respuesta.user?.role)
      throw new Error('El backend no devolvió una sesión JWT válida.');
    this.token = respuesta.accessToken;
    this.usuarioActual = {
      ...respuesta.user,
      role: this.normalizarRol(respuesta.user.role),
    };
    this.expira = this.leerExpiracionJWT(respuesta.accessToken);
    this.desafio = null;
    this.sesionSubject.next(this.usuarioActual);
  }
  private leerExpiracionJWT(token: string): number | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return typeof payload.exp === 'number' ? payload.exp * 1000 : null;
    } catch {
      return null;
    }
  }
  private normalizarRol(role: string): rolusuario {
    return role === 'ADMIN' || role === 'ROLE_ADMIN' ? 'ADMIN' : 'USER';
  }
  getToken(): string | null {
    return this.estaAutenticado() ? this.token : null;
  }
  getUsuarioActual(): usuarioautenticado | null {
    return this.estaAutenticado() ? this.usuarioActual : null;
  }
  getRole(): rolusuario | null {
    const actual = this.getUsuarioActual();
    return actual ? this.normalizarRol(actual.role) : null;
  }
  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }
  estaAutenticado(): boolean {
    if (!this.token || !this.usuarioActual) return false;
    if (this.expira !== null && Date.now() >= this.expira) {
      this.logout();
      return false;
    }
    return true;
  }
  logout(): void {
    this.token = null;
    this.usuarioActual = null;
    this.expira = null;
    this.desafio = null;
    this.recuperacionId = null;
    this.sesionSubject.next(null);
  }
  getHeaders(): HttpHeaders {
    const token = this.getToken();
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
  getPerfil(): Observable<usuarioautenticado> {
    return this.http.get<usuarioautenticado>(`${this.api}/me`, { headers: this.getHeaders() });
  }
  getUsuarios(): Observable<usuario[]> {
    return this.http.get<usuario[]>(this.usuariosApi, { headers: this.getHeaders() });
  }
  getUsuarioById(id: number): Observable<usuario> {
    return this.http.get<usuario>(`${this.usuariosApi}/${id}`, { headers: this.getHeaders() });
  }
  cambiarRol(id: number, role: rolusuario): Observable<usuario> {
    return this.http.patch<usuario>(
      `${this.usuariosApi}/${id}/role`,
      { role },
      { headers: this.getHeaders() },
    );
  }
  cambiarHabilitado(id: number, enabled: boolean): Observable<usuario> {
    return this.http.patch<usuario>(
      `${this.usuariosApi}/${id}/enabled`,
      { enabled },
      { headers: this.getHeaders() },
    );
  }
  // Compatibilidad con el uso antiguo de updateUsuario; el backend NO expone PUT /api/users/:id.
  updateUsuario(id: number, datos: Partial<usuario>): Observable<usuario> {
    if (datos.role === 'ADMIN' || datos.role === 'USER') return this.cambiarRol(id, datos.role);
    throw new Error('Solo se puede cambiar el rol o estado por las rutas PATCH disponibles.');
  }
  solicitarRecuperacion(email: string): Observable<respuestaMensaje> {
    return this.http.post<respuestaMensaje>(`${this.api}/forgot-password`, { email });
  }
  // El backend no devuelve challengeId en forgot-password (para evitar enumerar usuarios).
  // EmailService.java lo incluye en el correo de recuperación.
  prepararRecuperacion(challengeId: string): void {
    const limpio = challengeId.trim();
    if (!limpio) throw new Error('Ingresa el identificador recibido en el correo de recuperación.');
    this.recuperacionId = limpio;
  }
  restablecerPassword(code: string, newPassword: string): Observable<respuestaMensaje> {
    if (!this.recuperacionId)
      throw new Error('Falta el identificador de recuperación recibido por correo.');
    return this.http
      .post<respuestaMensaje>(`${this.api}/reset-password`, {
        challengeId: this.recuperacionId,
        code: code.trim(),
        newPassword,
      })
      .pipe(
        tap(() => {
          this.recuperacionId = null;
        }),
      );
  }
}
