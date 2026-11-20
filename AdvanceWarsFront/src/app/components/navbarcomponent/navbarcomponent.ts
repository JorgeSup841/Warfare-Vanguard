import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { usuarioservice } from '../../services/usuarioservice';

/** Barra de navegación de AURA WARS. */
@Component({
  selector: 'app-navbarcomponent',
  standalone: false,
  templateUrl: './navbarcomponent.html',
  styleUrl: './navbarcomponent.css',
})
export class Navbarcomponent implements OnInit {
  auth = inject(usuarioservice);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  /** Cada vez que se cambia de página, vuelve a revisar si hay sesión. */
  ngOnInit(): void {
    this.router.events.subscribe((evento) => {
      if (evento instanceof NavigationEnd) {
        this.cdr.markForCheck();
      }
    });
  }

  /** Cierra la sesión y regresa al inicio. */
  salir(): void {
    this.auth.logout();
    this.cdr.markForCheck();
    this.router.navigate(['/']);
  }
}
