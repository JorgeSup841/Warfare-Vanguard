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
}
