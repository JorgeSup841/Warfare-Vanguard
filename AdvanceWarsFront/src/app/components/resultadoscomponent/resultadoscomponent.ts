import { Component, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription, forkJoin } from 'rxjs';
import { tableroservice } from '../../services/tableroservice';
import { ReporteService } from '../../services/reporte.service';
import { partidaresumen, unidadservidor } from '../../models/tablero';
@Component({
  selector: 'app-resultadoscomponent',
  standalone: false,
  templateUrl: './resultadoscomponent.html',
  styleUrl: './resultadoscomponent.css',
})
export class Resultadoscomponent implements OnInit, OnDestroy {
  id = 0;
  partida: partidaresumen | null = null;
  unidades: unidadservidor[] = [];
  error = '';
  mensaje = '';
  cargando = false;
  private subs = new Subscription();
  constructor(
    private ruta: ActivatedRoute,
    private api: tableroservice,
    private pdf: ReporteService,
    private cdr: ChangeDetectorRef,
  ) {}
  ngOnInit(): void {
    this.id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.cargar();
  }
  cargar(): void {
    this.cargando = true;
    this.error = '';
    this.cdr.markForCheck();
    this.subs.add(
      forkJoin({
        partida: this.api.getPartida(this.id),
        unidades: this.api.getUnidades(this.id),
      }).subscribe({
        next: (r) => {
          this.partida = r.partida;
          this.unidades = r.unidades;
          this.cargando = false;
          this.cdr.markForCheck();
        },
        error: (e) => {
          this.cargando = false;
          this.error = `No se pudo consultar el resumen (HTTP ${e.status || 0}).`;
          this.cdr.markForCheck();
        },
      }),
    );
  }
  descargar(): void {
    this.subs.add(
      this.pdf.obtener(this.id).subscribe({
        next: (blob) => {
          try {
            this.pdf.guardarArchivo(blob, this.id);
            this.mensaje = 'PDF descargado.';
          } catch (e) {
            this.error = e instanceof Error ? e.message : 'PDF inválido.';
          }
          this.cdr.markForCheck();
        },
        error: (e) => {
          this.error = `No se pudo descargar el PDF (HTTP ${e.status || 0}).`;
          this.cdr.markForCheck();
        },
      }),
    );
  }
  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
