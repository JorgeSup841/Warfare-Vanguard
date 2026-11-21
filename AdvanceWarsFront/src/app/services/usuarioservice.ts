import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, throwError } from 'rxjs';

import { API_BASE_URL } from './api-config';

import {
  registro,
  respuestapendiente,
  verificacion,
  respuestalogin,
  desafioautenticacion,
  usuarioautenticado,
  rolusuario,
  respuestaMensaje,
} from '../models/usuario';

/** Gestiona registro, autenticación OTP, sesión JWT y recuperación de cuentas. */
@Injectable({
  providedIn: 'root',
})
export class usuarioservice {
  private readonly api = `${API_BASE_URL}/auth`;

  private token: string | null = null;
  private usuarioActual: usuarioautenticado | null = null;
  private expira: number | null = null;

  private desafio: desafioautenticacion | null = null;
  private recuperacionId: string | null = null;

  private sesionSubject = new BehaviorSubject<usuarioautenticado | null>(null);

  readonly sesion$ = this.sesionSubject.asObservable();

  constructor(private http: HttpClient) {}
  /** Solicita el registro de una cuenta y la creación del desafío OTP. */
  registrar(datos: registro): Observable<respuestapendiente> {
    return this.http.post<respuestapendiente>(`${this.api}/register`, datos);
  }

  /** Solicita un desafío OTP después de validar las credenciales. */
  iniciarSesion(datos: { username: string; password: string }): Observable<respuestapendiente> {
    return this.http.post<respuestapendiente>(`${this.api}/login`, datos);
  }

  /** Conserva en memoria los datos del desafío OTP pendiente. */
  prepararDesafio(tipo: 'correo' | 'login', respuesta: respuestapendiente, correo?: string): void {
    if (!respuesta?.challengeId) {
      throw new Error('El servidor no devolvió un identificador de verificación.');
    }

    this.desafio = {
      tipo,
      challengeId: respuesta.challengeId,
      correo,
      expiresAt: respuesta.expiresAt,
    };
  }

  /** Obtiene el desafío de correo o inicio de sesión activo. */
  getDesafio(): desafioautenticacion | null {
    return this.desafio;
  }

  /** Descarta el desafío OTP pendiente. */
  limpiarDesafio(): void {
    this.desafio = null;
  }

  /** Construye el cuerpo para verificar un código OTP. */
  private datosCodigo(codigo: string, tipo: 'correo' | 'login'): verificacion {
    if (!this.desafio || this.desafio.tipo !== tipo) {
      throw new Error('No existe una verificación pendiente.');
    }

    return {
      challengeId: this.desafio.challengeId,
      code: codigo.trim(),
    };
  }

  /** Solicita la activación del correo con el código OTP. */
  verificarCorreo(codigo: string): Observable<respuestaMensaje> {
    return this.http.post<respuestaMensaje>(
      `${this.api}/verify-email`,
      this.datosCodigo(codigo, 'correo'),
    );
  }

  /** Verifica el código OTP e inicia la sesión JWT. */
  verificarLogin(codigo: string): Observable<respuestalogin> {
    return this.http
      .post<respuestalogin>(`${this.api}/verify-otp`, this.datosCodigo(codigo, 'login'))
      .pipe(tap((respuesta) => this.guardarSesion(respuesta)));
  }

  /** Solicita un nuevo código para el desafío actual. */
  reenviarCodigo(): Observable<respuestapendiente> {
    if (!this.desafio) {
      return throwError(() => new Error('No existe una verificación pendiente.'));
    }

    return this.http
      .post<respuestapendiente>(`${this.api}/resend-otp`, {
        challengeId: this.desafio.challengeId,
      })
      .pipe(
        tap((respuesta) => {
          if (this.desafio) {
            this.desafio = {
              ...this.desafio,
              challengeId: respuesta.challengeId,
              expiresAt: respuesta.expiresAt,
            };
          }
        }),
      );
  }

  /** Almacena el JWT y los datos del usuario únicamente en memoria. */
  private guardarSesion(respuesta: respuestalogin): void {
    if (!respuesta.accessToken || !respuesta.user?.role) {
      throw new Error('El servidor no devolvió una sesión JWT válida.');
    }

    this.token = respuesta.accessToken;

    this.usuarioActual = {
      ...respuesta.user,
      role: usuarioservice.normalizarRol(respuesta.user.role),
    };

    this.expira = usuarioservice.leerExpiracionJWT(respuesta.accessToken);

    this.desafio = null;

    this.sesionSubject.next(this.usuarioActual);
  }

  /** Extrae la expiración del token JWT en milisegundos. */
  private static leerExpiracionJWT(token: string): number | null {
    try {
      const contenido = token.split('.')[1];

      if (!contenido) {
        return null;
      }

      const base64 = contenido.replace(/-/g, '+').replace(/_/g, '/');

      const payload = JSON.parse(atob(base64));

      return typeof payload.exp === 'number' ? payload.exp * 1000 : null;
    } catch {
      return null;
    }
  }

  /** Convierte los nombres de rol del backend al formato del frontend. */
  private static normalizarRol(role: string): rolusuario {
    return role === 'ADMIN' || role === 'ROLE_ADMIN' ? 'ADMIN' : 'USER';
  }

  /** Retorna el JWT únicamente si la sesión sigue siendo válida. */
  getToken(): string | null {
    return this.estaAutenticado() ? this.token : null;
  }

  /** Devuelve el usuario de la sesión activa. */
  getUsuarioActual(): usuarioautenticado | null {
    return this.estaAutenticado() ? this.usuarioActual : null;
  }

  /** Consulta el rol normalizado del usuario autenticado. */
  getRole(): rolusuario | null {
    const actual = this.getUsuarioActual();

    return actual ? usuarioservice.normalizarRol(actual.role) : null;
  }

  /** Determina si la sesión actual tiene permisos administrativos. */
  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  /** Comprueba la existencia y vigencia de una sesión JWT. */
  estaAutenticado(): boolean {
    if (!this.token || !this.usuarioActual) {
      return false;
    }

    if (this.expira !== null && Date.now() >= this.expira) {
      this.logout();
      return false;
    }

    return true;
  }

  /** Limpia la información de autenticación almacenada en memoria. */
  logout(): void {
    this.token = null;
    this.usuarioActual = null;
    this.expira = null;
    this.desafio = null;
    this.recuperacionId = null;

    this.sesionSubject.next(null);
  }

  /** Prepara las cabeceras de autorización para llamadas protegidas. */
  getHeaders(): HttpHeaders {
    const token = this.getToken();

    return token
      ? new HttpHeaders({
        Authorization: `Bearer ${token}`,
      })
      : new HttpHeaders();
  }

  /** Consulta el perfil autenticado con el servidor. */
  getPerfil(): Observable<usuarioautenticado> {
    return this.http.get<usuarioautenticado>(`${this.api}/me`, {
      headers: this.getHeaders(),
    });
  }

  /** Solicita al servidor el restablecimiento de contraseña. */
  solicitarRecuperacion(email: string): Observable<respuestaMensaje> {
    return this.http.post<respuestaMensaje>(`${this.api}/forgot-password`, {
      email: email.trim(),
    });
  }

  /** Guarda temporalmente el identificador de recuperación. */
  prepararRecuperacion(challengeId: string): void {
    const limpio = challengeId.trim();

    if (!limpio) {
      throw new Error('Ingresa el identificador recibido en el correo.');
    }

    this.recuperacionId = limpio;
  }

  /** Completa el restablecimiento de contraseña con código OTP. */
  restablecerPassword(code: string, newPassword: string): Observable<respuestaMensaje> {
    if (!this.recuperacionId) {
      return throwError(() => new Error('Falta el identificador de recuperación.'));
    }

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
