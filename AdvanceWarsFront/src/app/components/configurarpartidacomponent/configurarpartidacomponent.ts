import { Component, ChangeDetectorRef, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { tableroservice } from '../../services/tableroservice';
import { partidaresumen } from '../../models/tablero';
@Component({
  selector: 'app-configurarpartidacomponent',
  standalone: false,
  templateUrl: './configurarpartidacomponent.html',
  styleUrl: './configurarpartidacomponent.css',
})
export class Configurarpartidacomponent implements OnInit, OnDestroy {
  id = 0;
  resumen: partidaresumen | null = null;
  ocupado = false;
  error = '';
  mensaje = '';
  private sub = new Subscription();
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: tableroservice,
    private cdr: ChangeDetectorRef,
  ) {}
  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar();
  }
  cargar(): void {
    this.sub.add(
      this.api.getPartida(this.id).subscribe({
        next: (r) => {
          this.resumen = r;
          this.error = '';
          this.cdr.markForCheck();
        },
        error: (e) => {
          this.error = `No se encontró la partida (HTTP ${e.status || 0}).`;
          this.cdr.markForCheck();
        },
      }),
    );
  }
  iniciar(): void {
    if (this.ocupado || !this.resumen || this.resumen.status !== 'SETUP') return;
    if (!confirm('¿Iniciar la partida? Esta acción no se puede repetir.')) return;
    this.ocupado = true;
    this.error = '';
    this.cdr.markForCheck();
    this.sub.add(
      this.api.iniciarPartida(this.id).subscribe({
        next: (r) => {
          this.resumen = r;
          this.ocupado = false;
          this.cdr.markForCheck();
          void this.router.navigate(['/partidas', this.id, 'jugar']);
        },
        error: (e) => {
          this.ocupado = false;
          this.error = `No se pudo iniciar la partida (HTTP ${e.status || 0}).`;
          this.cdr.markForCheck();
        },
      }),
    );
  }
  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
}
