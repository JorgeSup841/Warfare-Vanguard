import { Component, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { usuarioservice } from '../../services/usuarioservice';
/** Registra cuentas y prepara la verificación del correo. */
@Component({
  selector: 'app-registercomponent',
  standalone: false,
  templateUrl: './registercomponent.html',
  styleUrl: './registercomponent.css',
})
export class Registercomponent implements OnDestroy {
  error = '';
  procesando = false;
  colorVisual = 'ROJO';
  private subs = new Subscription();
  constructor(
    private auth: usuarioservice,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}
  /** Valida el formulario y crea una cuenta en el servidor. */
  registrar(
    username: string,
    email: string,
    password: string,
    confirm: string,
    acepta: boolean,
  ): void {
    if (this.procesando) return;
    this.error = '';
    if (username.trim().length < 3) this.error = 'El usuario debe tener mínimo 3 caracteres.';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim()))
      this.error = 'El correo no es válido.';
    else if (password.length < 12) this.error = 'La contraseña debe tener mínimo 12 caracteres.';
    else if (password !== confirm) this.error = 'Las contraseñas no coinciden.';
    else if (!acepta) this.error = 'Debes aceptar los términos del servicio.';
    if (this.error) {
      this.cdr.markForCheck();
      return;
    }
    this.procesando = true;
    this.cdr.markForCheck();
    this.subs.add(
      this.auth.registrar({ username: username.trim(), email: email.trim(), password }).subscribe({
        next: (r) => {
          this.procesando = false;
          try {
            this.auth.prepararDesafio('correo', r, email.trim());
            this.router.navigate(['/verificar']).catch(() => { this.error = 'No se pudo abrir la pantalla solicitada.'; this.cdr.markForCheck(); });
          } catch (e) {
            this.error = e instanceof Error ? e.message : 'Respuesta de registro inválida.';
          }
          this.cdr.markForCheck();
        },
        error: (e: HttpErrorResponse) => {
          this.procesando = false;
          this.error =
            e.status === 409
              ? 'Usuario o correo ya registrado.'
              : e.status === 429
                ? 'Espera antes de intentarlo de nuevo.'
                : e.status === 0
                  ? 'No se pudo conectar con el servidor.'
                  : 'No fue posible registrar la cuenta.';
          this.cdr.markForCheck();
        },
      }),
    );
  }
  /** Libera los recursos y suscripciones del componente. */
  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
