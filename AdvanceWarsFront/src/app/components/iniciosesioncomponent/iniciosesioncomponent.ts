import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { Router } from '@angular/router';
import { usuarioservice } from '../../services/usuarioservice';

@Component({
  selector: 'app-iniciosesioncomponent',
  standalone: false,
  styleUrl: './iniciosesioncomponent.css',
  templateUrl: './iniciosesioncomponent.html',
})
/**
 * Componente para el inicio de sesion de los usuarios en el aplicativo
 */
export class Iniciosesioncomponent {
  private service = inject(usuarioservice);
  private router = inject(Router);
  private cd = inject(ChangeDetectorRef);

  error = '';

  /**
   * Envía usuario y contraseña. Si están bien, lleva a la pantalla del código.
   * @param usuario nombre de usuario
   * @param password contraseña
   */
  ingresar(usuario: string, password: string) {
    if (usuario.trim() === '' || password === '') {
      this.error = 'Escribe tu usuario y tu contraseña.';
      return;
    }
    this.error = '';

    this.service.iniciarSesion({ username: usuario.trim(), password }).subscribe({
      next: respuesta => {
        // Se guarda lo que necesita la pantalla del código.
        localStorage.setItem('challengeId', respuesta.challengeId);
        localStorage.setItem('tipoVerificacion', 'login');
        this.router.navigate(['/verificar']);
      },
      error: e => {
        if (e.status === 401) {
          this.error = 'Usuario o contraseña incorrectos, o la cuenta no está verificada.';
        } else if (e.status === 429) {
          this.error = 'Demasiados intentos. Espera un momento.';
        } else if (e.status === 0) {
          this.error = 'No se pudo conectar con el servidor.';
        } else {
          this.error = 'No se pudo iniciar sesión.';
        }
        this.cd.detectChanges();
      }
    });
  }
}
