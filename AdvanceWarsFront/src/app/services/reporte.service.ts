import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from './api-config';
import { usuarioservice } from './usuarioservice';
import { Observable } from 'rxjs';
import { tableroservice } from './tableroservice';
/** Gestiona la obtención y descarga de reportes PDF de partidas. */
@Injectable({ providedIn: 'root' })
export class ReporteService {
  private readonly prefijoNombreArchivo = 'aura-wars-partida';

  private readonly tablero = inject(tableroservice);
  private readonly http = inject(HttpClient);
  private readonly auth = inject(usuarioservice);
  /** Solicita el reporte PDF de una partida al servidor. */
  obtener(gameId: number): Observable<Blob> {
    return this.tablero.descargarPDF(gameId);
  }

  /** Solicita como ADMIN el PDF de cualquier partida. */
  obtenerComoAdmin(gameId: number): Observable<Blob> {
    return this.http.get(`${API_BASE_URL}/admin/games/${gameId}/report.pdf`, {
      headers: this.auth.getHeaders(),
      responseType: 'blob',
    });
  }

  /** Verifica la respuesta y descarga el informe PDF en el navegador. */
  guardarArchivo(blob: Blob, gameId: number): void {
    if (blob.type && !blob.type.includes('pdf') && !blob.type.includes('octet-stream'))
      throw new Error('El servidor no devolvió un PDF.');
    const url = URL.createObjectURL(blob);
    try {
      const enlace = document.createElement('a');
      enlace.href = url;
      enlace.download = `${this.prefijoNombreArchivo}-${gameId}.pdf`;
      document.body.appendChild(enlace);
      enlace.click();
      enlace.remove();
    } finally {
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    }
  }
}
