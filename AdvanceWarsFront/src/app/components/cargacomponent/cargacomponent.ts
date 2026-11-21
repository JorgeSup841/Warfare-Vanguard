import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

/** Pantalla de bienvenida que se muestra al abrir la aplicación. */
@Component({
  selector: 'app-cargacomponent',
  standalone: false,
  templateUrl: './cargacomponent.html',
  styleUrl: './cargacomponent.css'
})
export class Cargacomponent {

  private readonly router = inject(Router);

  /** Indica si la pantalla de bienvenida se está mostrando. */
  visible = true;

  /** Cierra la pantalla de bienvenida y lleva al inicio de sesión. */
  entrar(): void {
    this.visible = false;

    this.router.navigate(['/']).catch(() => undefined);
  }
}
