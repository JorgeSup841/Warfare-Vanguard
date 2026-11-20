import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { tableroservice } from '../../services/tableroservice';
import { partidaresumen } from '../../models/tablero';

/** Muestra los datos de una partida nueva y permite iniciarla. */
@Component({
  selector: 'app-configurarpartidacomponent',
  standalone: false,
  templateUrl: './configurarpartidacomponent.html',
  styleUrl: './configurarpartidacomponent.css',
})
export class Configurarpartidacomponent implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(tableroservice);
  private cdr = inject(ChangeDetectorRef);

  id = 0;
  partida: partidaresumen | null = null;
  iniciando = false;
  confirmar = false;
  mensaje = '';

  /** Lee el id de la ruta y carga la partida. */
  ngOnInit(): void {
    this.id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.cargar();
  }

  /** Pide al back los datos de la partida. */
  cargar(): void {
    this.servicio.getPartida(this.id).subscribe({
      next: (p) => {
        this.partida = p;
        this.cdr.detectChanges();
      },
      error: (e) => {
        this.mensaje = e.error?.message || 'No se encontró la partida';
        this.cdr.detectChanges();
      },
    });
  }

  /** Muestra la pregunta de confirmación. */
  solicitarInicio(): void {
    this.confirmar = true;
  }

  /** Oculta la pregunta de confirmación. */
  cancelarInicio(): void {
    this.confirmar = false;
  }

  /** Pide al back iniciar la partida y abre el tablero. */
  confirmarInicio(): void {
    this.iniciando = true;
    this.confirmar = false;
    this.mensaje = '';
    this.servicio.iniciarPartida(this.id).subscribe({
      next: () => {
        this.iniciando = false;
        this.router.navigate(['/partidas', this.id, 'jugar']);
      },
      error: (e) => {
        this.iniciando = false;
        this.mensaje = e.error?.message || 'No se pudo iniciar la partida';
        this.cdr.detectChanges();
      },
    });
  }
}
