import { Component, ChangeDetectorRef, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { tableroservice } from '../../services/tableroservice';
import { partidaresumen } from '../../models/tablero';
/** Configura e inicia partidas registradas en Spring Boot. */
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
  confirmacionInicio = false;
  error = '';
  mensaje = '';
  private sub = new Subscription();
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: tableroservice,
    private cdr: ChangeDetectorRef,
  ) {}
  /** Inicializa y carga los datos de la pantalla. */
  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar();
  }
  /** Consulta los datos necesarios para presentar la pantalla. */
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
  /** Solicita confirmación para iniciar la partida. */
  iniciar(): void {
    if (this.ocupado || !this.resumen || this.resumen.status !== 'SETUP') return;
    this.confirmacionInicio = true;
    this.cdr.markForCheck();
  }
  cancelarInicio(): void {
    this.confirmacionInicio = false;
    this.cdr.markForCheck();
  }
  confirmarInicio(): void {
    if (
      !this.confirmacionInicio ||
      this.ocupado ||
      !this.resumen ||
      this.resumen.status !== 'SETUP'
    )
      return;
    this.confirmacionInicio = false;
    this.ocupado = true;
    this.error = '';
    this.cdr.markForCheck();
    this.sub.add(
      this.api.iniciarPartida(this.id).subscribe({
        next: (r) => {
          this.resumen = r;
          this.ocupado = false;
          this.cdr.markForCheck();
          this.router.navigate(['/partidas', this.id, 'jugar']).catch(() => {
            this.error = 'No se pudo abrir la pantalla solicitada.';
            this.cdr.markForCheck();
          });
        },
        error: (e) => {
          this.ocupado = false;
          this.error = `No se pudo iniciar la partida (HTTP ${e.status || 0}).`;
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
