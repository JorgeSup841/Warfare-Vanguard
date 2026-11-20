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

  /** Muestra la confirmación antes de iniciar una partida válida. */
  solicitarInicio(): void {
    if (this.iniciando || this.partida?.status !== 'SETUP') {
      return;
    }
    this.confirmar = true;
    this.mensaje = '';
  }

  /** Confirma el inicio y delega la transición al servidor. */
  confirmarInicio(): void {
    if (!this.confirmar) {
      return;
    }
    this.iniciar();
  }

  /** Cierra la confirmación sin realizar solicitudes HTTP. */
  cancelarInicio(): void {
    if (!this.iniciando) {
      this.confirmar = false;
    }
  }

  /** Pide al back iniciar la partida y abre el tablero. */
  iniciar(): void {
    if (this.iniciando || this.partida?.status !== 'SETUP' || !this.confirmar) {
      return;
    }
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
