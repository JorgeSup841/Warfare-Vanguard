import { Component, ChangeDetectorRef, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { adminservice } from '../../services/adminservice';
import { ReporteService } from '../../services/reporte.service';
import { usuario, rolusuario } from '../../models/usuario';
/** Panel de administración de usuarios y reportes. */
@Component({ selector: 'app-admincomponent', standalone: false,
  templateUrl: './admincomponent.html', styleUrl: './admincomponent.css' })
export class Admincomponent implements OnInit, OnDestroy {
  usuarios: usuario[] = [];
  mensaje = '';
  error = '';
  procesando = false;
  idPDF = 0;
  confirmacion: { tipo: 'rol' | 'estado'; usuario: usuario; valor: rolusuario | boolean; texto: string } | null = null;
  private subs = new Subscription();
  constructor(private admin: adminservice, private pdf: ReporteService, private cdr: ChangeDetectorRef) {}
  /** Inicializa y carga los datos de la pantalla. */
  ngOnInit(): void { this.cargar(); }
  /** Consulta los datos necesarios para presentar la pantalla. */
  cargar(): void {
    this.error = ''; this.mensaje = '';
    this.subs.add(this.admin.listarUsuarios().subscribe({
      next: u => { this.usuarios = u; this.cdr.markForCheck(); },
      error: e => { this.error = `No se pudieron cargar usuarios: HTTP ${e.status || 0}. Solo ADMIN.`; this.cdr.markForCheck(); }
    }));
  }
  /** Prepara la confirmación del cambio de rol. */
  cambiarRol(u: usuario, nuevo: rolusuario): void {
    if (this.procesando || u.id == null || nuevo === u.role) return;
    this.confirmacion = {
      tipo: 'rol', usuario: u, valor: nuevo,
      texto: `¿Cambiar el rol de ${u.username} a ${nuevo}?`
    };
    this.cdr.markForCheck();
  }
  /** Prepara la confirmación del cambio de estado del usuario. */
  cambiarEstado(u: usuario, enabled: boolean): void {
    if (this.procesando || u.id == null || u.enabled === enabled) return;
    this.confirmacion = {
      tipo: 'estado', usuario: u, valor: enabled,
      texto: `¿${enabled ? 'Habilitar' : 'Deshabilitar'} a ${u.username}?`
    };
    this.cdr.markForCheck();
  }
  cancelarCambio(): void {
    this.confirmacion = null;
    this.cdr.markForCheck();
  }
  confirmarCambio(): void {
    const pendiente = this.confirmacion;
    if (!pendiente || pendiente.usuario.id == null || this.procesando) return;
    this.confirmacion = null;
    this.procesando = true;
    this.error = '';
    this.mensaje = '';
    this.cdr.markForCheck();
    if (pendiente.tipo === 'rol') {
      this.subs.add(this.admin.cambiarRol(pendiente.usuario.id, pendiente.valor as rolusuario).subscribe({
        next: r => {
          pendiente.usuario.role = r.role;
          this.procesando = false;
          this.mensaje = 'Rol actualizado por Spring Boot.';
          this.cdr.markForCheck();
        },
        error: e => {
          this.procesando = false;
          this.error = `No se cambió el rol (HTTP ${e.status || 0}).`;
          this.cdr.markForCheck();
        }
      }));
    } else {
      this.subs.add(this.admin.cambiarEstado(pendiente.usuario.id, pendiente.valor as boolean).subscribe({
        next: r => {
          pendiente.usuario.enabled = r.enabled;
          this.procesando = false;
          this.mensaje = 'Estado actualizado por Spring Boot.';
          this.cdr.markForCheck();
        },
        error: e => {
          this.procesando = false;
          this.error = `No se cambió el estado (HTTP ${e.status || 0}).`;
          this.cdr.markForCheck();
        }
      }));
    }
  }
  /** Descarga el informe PDF de una partida. */
  pdfPartida(): void {
    if (!Number.isSafeInteger(this.idPDF) || this.idPDF <= 0) {
      this.error = 'Introduce un ID de partida válido.'; this.cdr.markForCheck(); return;
    }
    this.subs.add(this.pdf.obtener(this.idPDF).subscribe({
      next: blob => { try { this.pdf.guardarArchivo(blob, this.idPDF); this.mensaje = 'PDF descargado.'; }
        catch (e) { this.error = e instanceof Error ? e.message : 'PDF inválido.'; } this.cdr.markForCheck(); },
      error: e => { this.error = `No se pudo descargar el PDF: HTTP ${e.status || 0}.`; this.cdr.markForCheck(); }
    }));
  }
  /** Libera los recursos y suscripciones del componente. */
  ngOnDestroy(): void { this.subs.unsubscribe(); }
}
