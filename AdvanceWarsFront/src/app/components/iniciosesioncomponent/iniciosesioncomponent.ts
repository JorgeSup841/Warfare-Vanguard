import { ChangeDetectorRef, Component, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { usuarioservice } from '../../services/usuarioservice';
/** Gestiona el inicio de sesión mediante correo OTP. */
@Component({
  selector: 'app-iniciosesioncomponent',
  standalone: false,
  templateUrl: './iniciosesioncomponent.html',
  styleUrl: './iniciosesioncomponent.css',
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
    private cdr: ChangeDetectorRef,
  ) {}
  /** Valida las credenciales y solicita el desafío OTP. */
  ingresar(username: string, password: string): void {
    if (this.procesando) return;
    this.error = '';
    this.mensaje = '';
    if (!username.trim() || !password) {
      this.error = 'Introduce usuario y contraseña.';
      this.cdr.markForCheck();
      return;
    }
    this.procesando = true;
    this.cdr.markForCheck();
    this.subs.add(
      this.auth.iniciarSesion({ username: username.trim(), password }).subscribe({
        next: (r) => {
          this.procesando = false;
          try {
            this.auth.prepararDesafio('login', r);
            this.router.navigate(['/verificar']).catch(() => { this.error = 'No se pudo abrir la pantalla solicitada.'; this.cdr.markForCheck(); });
          } catch (e) {
            this.error = e instanceof Error ? e.message : 'Respuesta OTP inválida.';
          }
          this.cdr.markForCheck();
        },
        error: (e: HttpErrorResponse) => {
          this.procesando = false;
          this.error =
            e.status === 0
              ? 'No se pudo conectar al backend.'
              : e.status === 429
                ? 'Demasiados intentos.'
                : e.status === 401
                  ? 'Credenciales incorrectas o cuenta no verificada.'
                  : 'No fue posible iniciar sesión.';
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
