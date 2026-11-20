import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { Router } from '@angular/router';
import { usuarioservice } from '../../services/usuarioservice';

@Component({
  selector: 'app-verificarcomponent',
  standalone: false,
  styleUrl: './verificarcomponent.css',
  templateUrl: './verificarcomponent.html',
})
/**
 * Pantalla para escribir el código de 6 dígitos que llegó al correo.
 * Sirve para dos cosas:
 *  - 'correo': activar la cuenta después del registro.
 *  - 'login': confirmar el inicio de sesión y recibir el token.
 */
export class Verificarcomponent {
  private service = inject(usuarioservice);
  private router = inject(Router);
  private cd = inject(ChangeDetectorRef);

  tipo = localStorage.getItem('tipoVerificacion') ?? 'correo';
  correo = localStorage.getItem('correoVerificar') ?? '';
  mensaje = '';
  error = '';
  verificado = false;

  /**
   * Envía el código al back.
   * @param codigo código de 6 dígitos
   */
  verificar(codigo: string) {
    this.mensaje = '';
    this.error = '';

    if (codigo.trim().length !== 6) {
      this.error = 'El código tiene 6 dígitos.';
      return;
    }

    const datos = { challengeId: localStorage.getItem('challengeId') ?? '', code: codigo.trim() };

    if (this.tipo === 'login') {
      // LOGIN: el back devuelve el token y se guarda la sesión.
      this.service.verificarLogin(datos).subscribe({
        next: respuesta => {
          localStorage.setItem('jwtToken', respuesta.accessToken);
          localStorage.setItem('userRole', respuesta.user.role);
          localStorage.setItem('username', respuesta.user.username);
          this.limpiar();
          this.router.navigate(['/juego']);
        },
        error: e => this.mostrarError(e.status)
      });
    } else {
      // REGISTRO: se activa la cuenta.
      this.service.verificarCorreo(datos).subscribe({
        next: () => {
          this.verificado = true;
          this.limpiar();
          this.cd.detectChanges();
        },
        error: e => this.mostrarError(e.status)
      });
    }
  }

  /**
   * Pide un código nuevo al correo.
   */
  reenviar() {
    this.mensaje = '';
    this.error = '';
    const challengeId = localStorage.getItem('challengeId') ?? '';

    this.service.reenviarCodigo(challengeId).subscribe({
      next: respuesta => {
        localStorage.setItem('challengeId', respuesta.challengeId);
        this.mensaje = 'Te enviamos un código nuevo.';
        this.cd.detectChanges();
      },
      error: () => {
        this.error = 'Espera un momento antes de pedir otro código.';
        this.cd.detectChanges();
      }
    });
  }

  /**
   * Muestra el mensaje de error según lo que respondió el back.
   * @param estado código HTTP del error
   */
  private mostrarError(estado: number) {
    this.error = estado === 429 ? 'Demasiados intentos. Espera un momento.' : 'Código inválido o vencido.';
    this.cd.detectChanges();
  }

  /**
   * Borra los datos temporales de la verificación.
   */
  private limpiar() {
    localStorage.removeItem('challengeId');
    localStorage.removeItem('correoVerificar');
    localStorage.removeItem('tipoVerificacion');
  }
}
