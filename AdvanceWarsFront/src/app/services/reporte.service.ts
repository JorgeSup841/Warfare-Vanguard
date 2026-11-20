import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { tableroservice } from './tableroservice';
@Injectable({ providedIn: 'root' })
export class ReporteService {
  constructor(private tablero: tableroservice) {}
  obtener(gameId: number): Observable<Blob> {
    return this.tablero.descargarPDF(gameId);
  }
  guardarArchivo(blob: Blob, gameId: number): void {
    if (blob.type && !blob.type.includes('pdf') && !blob.type.includes('octet-stream'))
      throw new Error('El servidor no devolvió un PDF.');
    const url = URL.createObjectURL(blob);
    try {
      const enlace = document.createElement('a');
      enlace.href = url;
      enlace.download = `aura-wars-partida-${gameId}.pdf`;
      document.body.appendChild(enlace);
      enlace.click();
      enlace.remove();
    } finally {
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    }
  }
}
