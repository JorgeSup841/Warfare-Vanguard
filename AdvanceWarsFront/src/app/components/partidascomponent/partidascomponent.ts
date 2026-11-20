import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { tableroservice } from '../../services/tableroservice';
import { partidaresumen } from '../../models/tablero';
/** Consulta y presenta las partidas registradas. */
@Component({
  selector: 'app-partidascomponent',
  standalone: false,
  templateUrl: './partidascomponent.html',
  styleUrl: './partidascomponent.css',
})
export class Partidascomponent implements OnInit, OnDestroy {
  partidas: partidaresumen[] = [];
  cargando = false;
  error = '';
  private sub = new Subscription();
  constructor(
    private service: tableroservice,
    private cdr: ChangeDetectorRef,
  ) {}
  /** Inicializa y carga los datos de la pantalla. */
  ngOnInit(): void {
    this.cargar();
  }
  /** Consulta los datos necesarios para presentar la pantalla. */
  cargar(): void {
    this.cargando = true;
    this.error = '';
    this.cdr.markForCheck();
    this.sub.add(
      this.service.listarPartidas().subscribe({
        next: (datos) => {
          this.partidas = Array.isArray(datos) ? datos : [];
          this.cargando = false;
          this.cdr.markForCheck();
        },
        error: (e) => {
          this.cargando = false;
          this.error = `No se pudo listar las partidas (HTTP ${e.status || 0}). GET /api/games debe estar disponible.`;
          this.cdr.markForCheck();
        },
      }),
    );
  }
  /** Libera los recursos y suscripciones del componente. */
  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
}
