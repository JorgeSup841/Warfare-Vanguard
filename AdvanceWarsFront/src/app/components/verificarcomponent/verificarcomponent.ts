import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, Subscription } from 'rxjs';
import { usuarioservice } from '../../services/usuarioservice';

@Component({
  selector: 'app-verificarcomponent',
  standalone: false,
  templateUrl: './verificarcomponent.html',
  styleUrl: './verificarcomponent.css',
})
export class Verificarcomponent implements OnDestroy {
  mensaje = '';
  error = '';
  procesando = false;
  verificado = false;
  private subs = new Subscription();

  constructor(
    public auth: usuarioservice,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  get tipo(): 'correo' | 'login' | null {
    return this.auth.getDesafio()?.tipo ?? null;
  }

  get correo(): string {
    return this.auth.getDesafio()?.correo ?? '';
  }

  verificar(codigo: string): void {
    if (this.procesando) return;
    this.mensaje = '';
    this.error = '';
    const limpio = codigo.trim();

    if (!/^\d{6}$/.test(limpio)) {
      this.error = 'Escribe el código de 6 dígitos recibido en tu correo.';
      this.cdr.markForCheck();
      return;
    }

    // Guardamos el tipo ANTES de suscribirnos. El servicio elimina el
    // desafío al terminar un login correcto.
    const tipoPendiente = this.tipo;
    if (!tipoPendiente) {
      this.error = 'No hay verificación pendiente. Vuelve al inicio.';
      this.cdr.markForCheck();
      return;
    }

    this.procesando = true;
    this.cdr.markForCheck();

    // Unificamos el tipo del observable para evitar TS2349. El usuario
    // introduce solamente el código; challengeId lo agrega usuarioservice.
    const peticion: Observable<unknown> =
      tipoPendiente === 'login'
        ? this.auth.verificarLogin(limpio)
        : this.auth.verificarCorreo(limpio);

    this.subs.add(
      peticion.subscribe({
        next: () => {
          this.procesando = false;
          if (tipoPendiente === 'correo') {
            this.verificado = true;
            this.auth.limpiarDesafio();
          } else {
            void this.router.navigate(['/panel']);
          }
          this.cdr.markForCheck();
        },
        error: (e: HttpErrorResponse) => {
          this.procesando = false;
          this.error =
            e.status === 429
              ? 'Demasiados intentos. Espera antes de reintentar.'
              : e.status === 0
                ? 'No hay conexión con el servidor.'
                : 'Código incorrecto o vencido.';
          this.cdr.markForCheck();
        },
      }),
    );
  }

  reenviar(): void {
    if (this.procesando || !this.tipo) return;
    this.procesando = true;
    this.error = '';
    this.mensaje = '';
    this.cdr.markForCheck();

    this.subs.add(
      this.auth.reenviarCodigo().subscribe({
        next: () => {
          this.procesando = false;
          this.mensaje = 'Enviamos un nuevo código a tu correo.';
          this.cdr.markForCheck();
        },
        error: () => {
          this.procesando = false;
          this.error = 'No se pudo reenviar el código. Espera antes de reintentar.';
          this.cdr.markForCheck();
        },
      }),
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
