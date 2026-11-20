import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { Router } from '@angular/router';
import { usuarioservice } from '../../services/usuarioservice';

@Component({
  selector: 'app-registercomponent',
  standalone: false,
  styleUrl: './registercomponent.css',
  templateUrl:'./registercomponent.html',
})
/**
 * Componente para el registro de nuevos usuarios en el aplicativo.
 */
export class Registercomponent {
  private service = inject(usuarioservice);
  private router = inject(Router);
  private cd = inject(ChangeDetectorRef);

  error = '';

  /**
   * Valida los datos, registra al usuario y lo lleva a verificar su correo.
   * @param usuario nombre de usuario
   * @param correo correo electrónico
   * @param password contraseña
   * @param confirm confirmación de la contraseña
   * @param acepta si marcó los términos
   */
  registrar(usuario: string, correo: string, password: string, confirm: string, acepta: boolean) {
    if (usuario.trim().length < 3) {
      this.error = 'El usuario debe tener mínimo 3 caracteres.';
      return;
    }
    if (!correo.includes('@')) {
      this.error = 'Escribe un correo válido.';
      return;
    }
    if (password.length < 12) {
      this.error = 'La contraseña debe tener mínimo 12 caracteres.';
      return;
    }
    if (password !== confirm) {
      this.error = 'Las contraseñas no coinciden.';
      return;
    }
    if (!acepta) {
      this.error = 'Debes aceptar los términos de la misión.';
      return;
    }
    this.error = '';

    this.service.registrar({ username: usuario.trim(), email: correo.trim(), password }).subscribe({
      next: respuesta => {
        // Se guarda lo que necesita la pantalla de verificación.
        localStorage.setItem('challengeId', respuesta.challengeId);
        localStorage.setItem('correoVerificar', correo.trim());
        localStorage.setItem('tipoVerificacion', 'correo');
        this.router.navigate(['/verificar']);
      },
      error: e => {
        if (e.status === 409) {
          this.error = 'Ese usuario o correo ya está registrado.';
        } else if (e.status === 429) {
          this.error = 'Demasiados intentos. Espera un momento.';
        } else if (e.status === 503) {
          this.error = 'No se pudo enviar el correo. Intenta de nuevo.';
        } else {
          this.error = 'No se pudo registrar el usuario.';
        }
        this.cd.detectChanges();
      }
    });
  }
}
