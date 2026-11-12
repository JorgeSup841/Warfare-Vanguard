import { Component } from '@angular/core';

@Component({
  selector: 'app-registercomponent',
  standalone: false,
  styleUrl: './registercomponent.css',
  templateUrl: './registercomponent.html',
})
export class Registercomponent {
  error = '';

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
