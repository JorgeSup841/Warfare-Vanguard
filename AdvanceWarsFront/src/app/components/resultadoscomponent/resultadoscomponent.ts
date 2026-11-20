import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';
import { tableroservice } from '../../services/tableroservice';
import { ReporteService } from '../../services/reporte.service';
import { partidaresumen, unidadservidor } from '../../models/tablero';

/** Muestra el resultado de una partida y permite descargar el PDF. */
@Component({
  selector: 'app-resultadoscomponent',
  standalone: false,
  templateUrl: './resultadoscomponent.html',
  styleUrl: './resultadoscomponent.css',
})
export class Resultadoscomponent implements OnInit {
  private ruta = inject(ActivatedRoute);
  private servicio = inject(tableroservice);
  private pdf = inject(ReporteService);
  private cdr = inject(ChangeDetectorRef);

  id = 0;
  partida: partidaresumen | null = null;
  unidades: unidadservidor[] = [];
  cargando = false;
  descargando = false;
  mensaje = '';
  error = '';

  /** Lee el id de la ruta y carga los resultados. */
  ngOnInit(): void {
    this.id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.cargar();
  }

  /** Pide al back la partida y sus unidades. */
  cargar(): void {
    this.cargando = true;
    forkJoin({
      partida: this.servicio.getPartida(this.id),
      unidades: this.servicio.getUnidades(this.id),
    }).subscribe({
      next: (r) => {
        this.partida = r.partida;
        this.unidades = r.unidades;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (e) => {
        this.cargando = false;
        this.error = e.error?.message || 'No se pudieron cargar los resultados';
        this.cdr.detectChanges();
      },
    });
  }

  /** Nombre del ganador, si ya lo hay. */
  get ganador(): string {
    if (!this.partida) return '';
    const g = this.partida.players.find((j) => j.status === 'WINNER');
    return g ? g.name : '';
  }

  /** Cuántas unidades le quedan a un jugador. */
  unidadesDe(orden: number): number {
    return this.unidades.filter((u) => u.ownerOrder === orden).length;
  }

  /** Descarga el informe PDF que genera el back. */
  descargar(): void {
    this.descargando = true;
    this.mensaje = '';
    this.error = '';
    this.pdf.obtener(this.id).subscribe({
      next: (archivo) => {
        this.pdf.guardarArchivo(archivo, this.id);
        this.descargando = false;
        this.mensaje = 'PDF descargado';
        this.cdr.detectChanges();
      },
      error: (e) => {
        this.descargando = false;
        this.error = `No se pudo descargar el PDF (error ${e.status || 0})`;
        this.cdr.detectChanges();
      },
    });
  }
}
