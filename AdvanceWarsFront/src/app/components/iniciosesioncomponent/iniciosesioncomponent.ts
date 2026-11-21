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
            this.router.navigate(['/verificar']).catch(() => {
              this.error = 'No se pudo abrir la pantalla solicitada.';
              this.cdr.markForCheck();
            });
          } catch (e) {
            this.error = e instanceof Error ? e.message : 'Respuesta OTP inválida.';
          }
          this.cdr.markForCheck();
        },
        error: (e: HttpErrorResponse) => {
          this.procesando = false;
          this.error = Iniciosesioncomponent.obtenerErrorInicio(e);
          this.cdr.markForCheck();
        },
      }),
    );
  }
  /** Diferencia los límites temporales enviados por el backend sin alterar la seguridad. */
  private static obtenerErrorInicio(e: HttpErrorResponse): string {
    const mensajeServidor = typeof e.error?.message === 'string'
      ? e.error.message.trim()
      : '';

    if (e.status === 0) return 'No se pudo conectar al backend.';
    if (e.status === 429) {
      if (mensajeServidor === 'Demasiados intentos') {
        return 'Se alcanzó el límite de contraseñas incorrectas. Espera a que termine el bloqueo temporal de 15 minutos antes de volver a intentarlo.';
      }
      if (mensajeServidor.startsWith('Demasiadas solicitudes')) {
        return 'Se alcanzó el límite de solicitudes desde esta conexión. Espera a que termine la ventana de 1 minuto.';
      }
      if (mensajeServidor.startsWith('Espera antes de solicitar otro código')) {
        return 'Ya se solicitó un código de acceso recientemente. Espera al menos 1 minuto antes de solicitar otro.';
      }
      if (mensajeServidor.startsWith('Demasiados códigos solicitados')) {
        return 'Se alcanzó el límite de códigos OTP. Espera a que termine la ventana de 1 hora antes de solicitar otro.';
      }
      return mensajeServidor || 'Demasiadas solicitudes. Espera antes de volver a intentarlo.';
    }
    if (e.status === 401) return 'Credenciales incorrectas o cuenta no verificada.';
    if (e.status === 503) return 'El servidor no pudo enviar el código de verificación por correo.';
    return 'No fue posible iniciar sesión.';
  }

  /** Libera los recursos y suscripciones del componente. */
  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
