
import { Component, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { usuarioservice } from '../../services/usuarioservice';

/**
 * Gestiona el registro de nuevos usuarios en AURA WARS.
 * Valida los datos del formulario, solicita la creación
 * de la cuenta y prepara la verificación por correo electrónico.
 */
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

  /**
   * Valida el formulario y registra una cuenta en Spring Boot.
   * Si el registro es correcto, prepara el desafío OTP y
   * redirige al usuario a la pantalla de verificación.
   *
   * @param username Nombre de usuario.
   * @param email Correo electrónico.
   * @param password Contraseña.
   * @param confirm Confirmación de contraseña.
   * @param acepta Indica si se aceptaron los términos del servicio.
   */
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

    const usuarioLimpio = username.trim();
    const correoLimpio = email.trim();

    if (usuarioLimpio.length < 3) {
      this.error = 'El usuario debe tener mínimo 3 caracteres.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correoLimpio)) {
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
        username: usuarioLimpio,
        email: correoLimpio,
        password
      }).subscribe({

        next: (respuesta) => {

          this.procesando = false;

          try {
            this.auth.prepararDesafio(
              'correo',
              respuesta,
              correoLimpio
            );

            this.router.navigate(['/verificar']).then(
              (navegado) => {

                if (!navegado) {
                  this.error = 'No se pudo abrir la pantalla de verificación.';
                  this.cdr.markForCheck();
                }

              }
            ).catch(() => {

              this.error = 'Ocurrió un error al abrir la pantalla de verificación.';
              this.cdr.markForCheck();

            });

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

  /**
   * Cancela las suscripciones activas al destruir
   * el componente para evitar fugas de memoria.
   */
  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
