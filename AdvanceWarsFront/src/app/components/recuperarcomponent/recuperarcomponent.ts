import { Component, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { usuarioservice } from '../../services/usuarioservice';
/** Permite recuperar el acceso mediante correo. */
@Component({ selector: 'app-recuperarcomponent', standalone: false,
  templateUrl: './recuperarcomponent.html', styleUrl: './recuperarcomponent.css' })
export class Recuperarcomponent implements OnDestroy {
  ocupado = false;
  mensaje = '';
  error = '';
  modo: 'solicitar' | 'restablecer' = 'solicitar';
  private subs = new Subscription();
  constructor(private auth: usuarioservice, private cdr: ChangeDetectorRef) {}
  /** Gestiona solicitar en AURA WARS. */
  solicitar(email: string): void {
    if (this.ocupado || !email.trim()) return;
    this.ocupado = true; this.error = ''; this.mensaje = '';
    this.cdr.markForCheck();
    this.subs.add(this.auth.solicitarRecuperacion(email.trim()).subscribe({
      next: () => {
        this.ocupado = false;
        this.mensaje = 'Si tu correo está registrado y verificado, recibirás un código y un identificador de recuperación.';
        this.modo = 'restablecer';
        this.cdr.markForCheck();
      },
      error: (e) => {
        this.ocupado = false;
        this.error = e.status === 0 ? 'No se pudo conectar con Spring Boot (puerto 8080).'
          : `No fue posible solicitar recuperación (HTTP ${e.status}).`;
        this.cdr.markForCheck();
      }
    }));
  }
  /** Gestiona restablecer en AURA WARS. */
  restablecer(id: string, codigo: string, clave: string): void {
    if (this.ocupado) return;
    this.error = ''; this.mensaje = '';
    if (!id.trim() || !/^\d{6}$/.test(codigo.trim()) || clave.length < 12) {
      this.error = 'Ingresa el identificador del correo, el código de 6 dígitos y la nueva contraseña (mínimo 12 caracteres).';
      this.cdr.markForCheck(); return;
    }
    try { this.auth.prepararRecuperacion(id); }
    catch (e) { this.error = e instanceof Error ? e.message : 'Datos inválidos.'; this.cdr.markForCheck(); return; }
    this.ocupado = true; this.cdr.markForCheck();
    this.subs.add(this.auth.restablecerPassword(codigo, clave).subscribe({
      next: (r) => { this.ocupado = false; this.mensaje = r.message || 'Contraseña actualizada. Vuelve al inicio.'; this.cdr.markForCheck(); },
      error: (e) => { this.ocupado = false; this.error = e.status === 0 ? 'Sin conexión con el backend.' : 'Código incorrecto, vencido o contraseña inválida.'; this.cdr.markForCheck(); }
    }));
  }
  /** Libera los recursos y suscripciones del componente. */
  ngOnDestroy(): void { this.subs.unsubscribe(); }
}
