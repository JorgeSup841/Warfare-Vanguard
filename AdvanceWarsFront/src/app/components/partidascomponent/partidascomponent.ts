import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { tableroservice } from '../../services/tableroservice';
import { partidaresumen } from '../../models/tablero';

/** Muestra la lista de partidas del usuario. */
@Component({
  selector: 'app-partidascomponent',
  standalone: false,
  templateUrl: './partidascomponent.html',
  styleUrl: './partidascomponent.css',
})
export class Partidascomponent implements OnInit {
  private servicio = inject(tableroservice);
  private cdr = inject(ChangeDetectorRef);

  partidas: partidaresumen[] = [];
  cargando = false;
  mensaje = '';

  estados: Record<string, string> = {
    SETUP: 'EN PREPARACIÓN',
    IN_PROGRESS: 'EN CURSO',
    FINISHED: 'TERMINADA',
  };

  filtro = 'TODAS';
  opciones = ['TODAS', 'SETUP', 'IN_PROGRESS', 'FINISHED'];

  /** Carga las partidas al abrir la pantalla. */
  ngOnInit(): void {
    this.cargar();
  }

  /** Pide al back la lista de mis partidas. */
  cargar(): void {
    this.cargando = true;
    this.mensaje = '';
    this.servicio.listarPartidas().subscribe({
      next: (datos) => {
        this.partidas = datos;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (e) => {
        this.cargando = false;
        this.mensaje = e.error?.message || 'No se pudieron cargar las partidas';
        this.cdr.detectChanges();
      },
    });
  }

  /** Partidas que coinciden con el filtro elegido. */
  get partidasFiltradas(): partidaresumen[] {
    if (this.filtro === 'TODAS') return this.partidas;
    return this.partidas.filter((p) => p.status === this.filtro);
  }

  /** Cuántas partidas hay en un estado. */
  contar(estado: string): number {
    if (estado === 'TODAS') return this.partidas.length;
    return this.partidas.filter((p) => p.status === estado).length;
  }
}
