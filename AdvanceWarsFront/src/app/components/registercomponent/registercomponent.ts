import { Component } from '@angular/core';

@Component({
  selector: 'app-registercomponent',
  standalone: false,
  styleUrl: './registercomponent.css',
  templateUrl: './registercomponent.html',
})
/**
 * Componente para el registro de nuevos usuarios en el aplicativo.
 */
export class Registercomponent {
  error = '';

  /**
   * Valida la contraseña antes de registrar al usuario.
   * Debe tener mínimo 12 caracteres y coincidir con la confirmación.
   * @param password contraseña escrita por el usuario
   * @param confirm confirmación de la contraseña
   */
  registrar(password: string, confirm: string) {
    if (password.length < 12) {
      this.error = 'La contraseña debe tener mínimo 12 caracteres.';
      return;
    }
    if (password !== confirm) {
      this.error = 'Las contraseñas no coinciden.';
      return;
    }
    this.error = '';

  }
}
