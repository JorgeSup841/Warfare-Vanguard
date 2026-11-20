
import { Component, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { usuarioservice } from '../../services/usuarioservice';

@Component({
  selector: 'app-registercomponent',
  standalone: false,
  templateUrl: './registercomponent.html',
  styleUrl: './registercomponent.css'
})
export class Registercomponent implements OnDestroy {

  error = '';
  procesando = false;
  colorVisual = 'ROJO';

  private subs = new Subscription();

  constructor(
    private auth: usuarioservice,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  registrar(
    username: string,
    email: string,
    password: string,
    confirm: string,
    acepta: boolean
  ): void {

    if (this.procesando) {
      return;
    }

    this.error = '';

    if (username.trim().length < 3) {
      this.error = 'El usuario debe tener mínimo 3 caracteres.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      this.error = 'Introduce un correo electrónico válido.';
    } else if (password.length < 12) {
      this.error = 'La contraseña debe tener mínimo 12 caracteres.';
    } else if (password !== confirm) {
      this.error = 'Las contraseñas no coinciden.';
    } else if (!acepta) {
      this.error = 'Debes aceptar los términos del servicio.';
    }

    if (this.error) {
      this.cdr.markForCheck();
      return;
    }

    this.procesando = true;
    this.cdr.markForCheck();

    this.subs.add(
      this.auth.registrar({
        username: username.trim(),
        email: email.trim(),
        password
      }).subscribe({

        next: (respuesta) => {

          this.procesando = false;

          try {

            this.auth.prepararDesafio(
              'correo',
              respuesta,
              email.trim()
            );

            void this.router.navigate(['/verificar']);

          } catch (error) {

            this.error = error instanceof Error
              ? error.message
              : 'La respuesta del registro no es válida.';
          }

          this.cdr.markForCheck();
        },

        error: (error: HttpErrorResponse) => {

          this.procesando = false;

          switch (error.status) {

            case 0:
              this.error = 'No se pudo conectar con el servidor.';
              break;

            case 400:
              this.error = 'Revisa los datos ingresados.';
              break;

            case 409:
              this.error = 'El usuario o correo ya está registrado.';
              break;

            case 429:
              this.error = 'Demasiados intentos. Intenta más tarde.';
              break;

            case 503:
              this.error = 'No se pudo enviar el correo de verificación.';
              break;

            default:
              this.error = 'No fue posible registrar la cuenta.';
          }

          this.cdr.markForCheck();
        }

      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
