import { Component, ChangeDetectorRef, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { adminservice } from '../../services/adminservice';
import { ReporteService } from '../../services/reporte.service';
import { usuario, rolusuario } from '../../models/usuario';
@Component({
  selector: 'app-admincomponent',
  standalone: false,
  templateUrl: './admincomponent.html',
  styleUrl: './admincomponent.css',
})
export class Admincomponent implements OnInit, OnDestroy {
  usuarios: usuario[] = [];
  mensaje = '';
  error = '';
  procesando = false;
  idPDF = 0;
  private subs = new Subscription();
  constructor(
    private admin: adminservice,
    private pdf: ReporteService,
    private cdr: ChangeDetectorRef,
  ) {}
  ngOnInit(): void {
    this.cargar();
  }
  cargar(): void {
    this.error = '';
    this.mensaje = '';
    this.subs.add(
      this.admin.listarUsuarios().subscribe({
        next: (u) => {
          this.usuarios = u;
          this.cdr.markForCheck();
        },
        error: (e) => {
          this.error = `No se pudieron cargar usuarios: HTTP ${e.status || 0}. Solo ADMIN.`;
          this.cdr.markForCheck();
        },
      }),
    );
  }
  cambiarRol(u: usuario, nuevo: rolusuario): void {
    if (this.procesando || u.id == null || nuevo === u.role) return;
    if (!confirm(`¿Cambiar el rol de ${u.username} a ${nuevo}?`)) return;
    this.procesando = true;
    this.error = '';
    this.cdr.markForCheck();
    this.subs.add(
      this.admin.cambiarRol(u.id, nuevo).subscribe({
        next: (r) => {
          u.role = r.role;
          this.procesando = false;
          this.mensaje = 'Rol actualizado por Spring Boot.';
          this.cdr.markForCheck();
        },
        error: (e) => {
          this.procesando = false;
          this.error = `No se cambió el rol (HTTP ${e.status || 0}).`;
          this.cdr.markForCheck();
        },
      }),
    );
  }
  cambiarEstado(u: usuario, enabled: boolean): void {
    if (this.procesando || u.id == null || u.enabled === enabled) return;
    if (!confirm(`¿${enabled ? 'Habilitar' : 'Deshabilitar'} a ${u.username}?`)) return;
    this.procesando = true;
    this.error = '';
    this.cdr.markForCheck();
    this.subs.add(
      this.admin.cambiarEstado(u.id, enabled).subscribe({
        next: (r) => {
          u.enabled = r.enabled;
          this.procesando = false;
          this.mensaje = 'Estado actualizado por Spring Boot.';
          this.cdr.markForCheck();
        },
        error: (e) => {
          this.procesando = false;
          this.error = `No se cambió el estado (HTTP ${e.status || 0}).`;
          this.cdr.markForCheck();
        },
      }),
    );
  }
  pdfPartida(): void {
    if (!Number.isSafeInteger(this.idPDF) || this.idPDF <= 0) {
      this.error = 'Introduce un ID de partida válido.';
      this.cdr.markForCheck();
      return;
    }
    this.subs.add(
      this.pdf.obtener(this.idPDF).subscribe({
        next: (blob) => {
          try {
            this.pdf.guardarArchivo(blob, this.idPDF);
            this.mensaje = 'PDF descargado.';
          } catch (e) {
            this.error = e instanceof Error ? e.message : 'PDF inválido.';
          }
          this.cdr.markForCheck();
        },
        error: (e) => {
          this.error = `No se pudo descargar el PDF: HTTP ${e.status || 0}.`;
          this.cdr.markForCheck();
        },
      }),
    );
  }
  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
