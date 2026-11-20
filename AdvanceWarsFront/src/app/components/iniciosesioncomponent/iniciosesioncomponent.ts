
import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { usuarioservice } from '../../services/usuarioservice';

@Component({
  selector: 'app-iniciosesioncomponent',
  standalone: false,
  templateUrl: './iniciosesioncomponent.html',
  styleUrl: './iniciosesioncomponent.css'
})
export class Iniciosesioncomponent implements OnDestroy {

  error = '';
  mensaje = '';
  procesando = false;
  mostrarClave = false;

  private subs = new Subscription();

  constructor(
    private auth: usuarioservice,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ingresar(username: string, password: string): void {

    if (this.procesando) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    if (!username.trim() || !password) {
      this.error = 'Introduce tu usuario y contraseña.';
      this.cdr.markForCheck();
      return;
    }

    this.procesando = true;
    this.cdr.markForCheck();

    this.subs.add(
      this.auth.iniciarSesion({
        username: username.trim(),
        password
      }).subscribe({

        next: (respuesta) => {

          this.procesando = false;

          try {

            this.auth.prepararDesafio('login', respuesta);

            void this.router.navigate(['/verificar']);

          } catch (error) {

            this.error = error instanceof Error
              ? error.message
              : 'La respuesta de verificación no es válida.';
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
              this.error = 'Verifica los datos ingresados.';
              break;

            case 401:
              this.error = 'Usuario o contraseña incorrectos, o cuenta no verificada.';
              break;

            case 403:
              this.error = 'No tienes permiso para acceder.';
              break;

            case 429:
              this.error = 'Demasiados intentos. Intenta más tarde.';
              break;

            default:
              this.error = 'No se pudo iniciar sesión.';
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
