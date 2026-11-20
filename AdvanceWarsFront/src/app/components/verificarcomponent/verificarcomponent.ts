import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { usuarioservice } from '../../services/usuarioservice';

@Component({
  selector: 'app-verificarcomponent',
  standalone: false,
  styleUrl: './verificarcomponent.css',
  templateUrl: './verificarcomponent.html',
})
/**
 * Pantalla para escribir el código de 6 dígitos que llegó al correo.
 */
export class Verificarcomponent {
  private service = inject(usuarioservice);
  private cd = inject(ChangeDetectorRef);

  correo = localStorage.getItem('correoVerificar') ?? '';
  mensaje = '';
  error = '';
  verificado = false;

  /**
   * Envía el código al back para activar la cuenta.
   * @param codigo código de 6 dígitos
   */
  verificar(codigo: string) {
    this.mensaje = '';
    this.error = '';

    if (codigo.trim().length !== 6) {
      this.error = 'El código tiene 6 dígitos.';
      return;
    }

    const challengeId = localStorage.getItem('challengeId') ?? '';

    this.service.verificarCorreo({ challengeId, code: codigo.trim() }).subscribe({
      next: () => {
        this.verificado = true;
        localStorage.removeItem('challengeId');
        localStorage.removeItem('correoVerificar');
        this.cd.detectChanges();
      },
      error: e => {
        this.error = e.status === 429 ? 'Demasiados intentos. Espera un momento.' : 'Código inválido o vencido.';
        this.cd.detectChanges();
      }
    });
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
}
