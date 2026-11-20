import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';
import { usuarioservice } from './usuarioservice';
import { usuario, rolusuario } from '../models/usuario';
/** Servicio de administración de usuarios a través de la API Spring Boot. */
@Injectable({ providedIn: 'root' })
export class adminservice {
  private readonly api = `${API_BASE_URL}/users`;
  constructor(
    private http: HttpClient,
    private auth: usuarioservice,
  ) {}
  /** Consulta los usuarios administrables con las credenciales actuales. */
  listarUsuarios(): Observable<usuario[]> {
    return this.http.get<usuario[]>(this.api, { headers: this.auth.getHeaders() });
  }

  /** Modifica el rol de un usuario mediante PATCH autenticado. */
  cambiarRol(id: number, role: rolusuario): Observable<usuario> {
    return this.http.patch<usuario>(
      `${this.api}/${id}/role`,
      { role },
      { headers: this.auth.getHeaders() },
    );
  }

  /** Habilita o deshabilita una cuenta mediante PATCH autenticado. */
  cambiarEstado(id: number, enabled: boolean): Observable<usuario> {
    return this.http.patch<usuario>(
      `${this.api}/${id}/enabled`,
      { enabled },
      { headers: this.auth.getHeaders() },
    );
  }
  // No existe /api/admin/game-configuration en AdvanceWars(3).zip.
}
