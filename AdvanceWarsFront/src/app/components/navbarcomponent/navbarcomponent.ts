import { Component, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { usuarioservice } from '../../services/usuarioservice';
/** Barra de navegación de AURA WARS. */
@Component({
  selector: 'app-navbarcomponent',
  standalone: false,
  templateUrl: './navbarcomponent.html',
  styleUrl: './navbarcomponent.css',
})
export class Navbarcomponent {
  constructor(
    public auth: usuarioservice,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}
  /** Cierra la sesión y regresa al inicio. */
  salir(): void {
    this.auth.logout();
    this.router.navigate(['/']).catch(() => this.cdr.markForCheck());
    this.cdr.markForCheck();
  }
}
