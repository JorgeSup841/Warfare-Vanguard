
import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, Subscription } from 'rxjs';
import { usuarioservice } from '../../services/usuarioservice';

@Component({
  selector: 'app-verificarcomponent',
  standalone: false,
  templateUrl: './verificarcomponent.html',
  styleUrl: './verificarcomponent.css'
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
    private cdr: ChangeDetectorRef
  ) {}

  get tipo(): 'correo' | 'login' | null {
    return this.auth.getDesafio()?.tipo ?? null;
  }

  get correo(): string {
    return this.auth.getDesafio()?.correo ?? '';
  }

  verificar(codigo: string): void {

    if (this.procesando) {
      return;
    }

    this.mensaje = '';
    this.error = '';

    const limpio = codigo.trim();

    if (!/^\d{6}$/.test(limpio)) {
      this.error = 'Escribe el código de 6 dígitos recibido en tu correo.';
      this.cdr.markForCheck();
      return;
    }

    const tipoPendiente = this.tipo;

    if (!tipoPendiente) {
      this.error = 'No hay una verificación pendiente. Vuelve al inicio.';
      this.cdr.markForCheck();
      return;
    }

    this.procesando = true;
    this.cdr.markForCheck();

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

            const destino = this.auth.isAdmin()
              ? '/admin'
              : '/panel';

            void this.router.navigate([destino]);
          }

          this.cdr.markForCheck();
        },

        error: (error: HttpErrorResponse) => {

          this.procesando = false;

          switch (error.status) {

            case 0:
              this.error = 'No hay conexión con el servidor.';
              break;

            case 400:
              this.error = 'El código ingresado no es válido.';
              break;

            case 401:
              this.error = 'El código es incorrecto o ha vencido.';
              break;

            case 429:
              this.error = 'Demasiados intentos. Espera antes de reintentar.';
              break;

            default:
              this.error = 'No se pudo verificar el código.';
          }

          this.cdr.markForCheck();
        }

      })
    );
  }

  reenviar(): void {

    if (this.procesando || !this.tipo) {
      return;
    }

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

        error: (error: HttpErrorResponse) => {

          this.procesando = false;

          this.error = error.status === 429
            ? 'Debes esperar antes de solicitar otro código.'
            : 'No se pudo reenviar el código. Intenta nuevamente.';

          this.cdr.markForCheck();
        }

      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
