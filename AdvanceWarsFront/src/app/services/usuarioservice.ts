import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { usuario, AuthRequest, AuthResponse, historial } from '../models/usuario';

@Injectable({
  providedIn: 'root'
})
export class usuarioservice {
  private baseUrl = 'http://localhost:8080';

  private http = inject(HttpClient);



  login(credentials: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, credentials);
  }

  register(usuario: usuario): Observable<string> {
    const nuevoUsuario = { ...usuario, role: 'USER' as const };
    return this.http.post(`${this.baseUrl}/auth/register`, nuevoUsuario, { responseType: 'text' });
  }



  saveSession(response: AuthResponse): void {
    localStorage.setItem('jwtToken', response.token);
    localStorage.setItem('userRole', response.role);
    localStorage.setItem('username', response.username);
  }

  getToken(): string | null {
    return localStorage.getItem('jwtToken');
  }

  getRole(): string | null {
    return localStorage.getItem('userRole');
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  logout(): void {
    localStorage.clear();
  }

  private getHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    });
  }



  getUsuarios(): Observable<usuario[]> {
    return this.http.get<usuario[]>(`${this.baseUrl}/user/getall`, { headers: this.getHeaders() });
  }

  getUsuarioById(id: number): Observable<usuario> {
    return this.http.get<usuario>(`${this.baseUrl}/user/getbyid/${id}`, { headers: this.getHeaders() });
  }

  updateUsuario(id: number, usuario: usuario): Observable<usuario> {
    return this.http.put<usuario>(`${this.baseUrl}/user/updatejson?id=${id}`, usuario, { headers: this.getHeaders() });
  }

  deleteUsuario(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/user/deletebyid/${id}`, { headers: this.getHeaders() });
  }



  guardarPartida(resultado: historial): Observable<historial> {
    return this.http.post<historial>(`${this.baseUrl}/historial/save`, resultado, { headers: this.getHeaders() });
  }

  getHistorial(): Observable<historial[]> {
    return this.http.get<historial[]>(`${this.baseUrl}/historial/mine`, { headers: this.getHeaders() });
  }
}
