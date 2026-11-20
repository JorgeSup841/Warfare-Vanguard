import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { tableroservice } from '../../services/tableroservice';
import { partidaresumen } from '../../models/tablero';
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
  ngOnInit(): void {
    this.cargar();
  }
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
  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
}
