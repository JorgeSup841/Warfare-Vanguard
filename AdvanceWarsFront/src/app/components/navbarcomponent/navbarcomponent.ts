
import { Component, ChangeDetectorRef, OnInit, OnDestroy, inject } from '@angular/core';

import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { usuarioservice } from '../../services/usuarioservice';

/** Gestiona la barra de navegación de AURA WARS. */
@Component({
  selector: 'app-navbarcomponent',
  standalone: false,
  templateUrl: './navbarcomponent.html',
  styleUrl: './navbarcomponent.css'
})
export class Navbarcomponent implements OnInit, OnDestroy {

  readonly auth = inject(usuarioservice);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly subs = new Subscription();

  /** Actualiza la barra cuando cambia la sesión. */
  ngOnInit(): void {
    this.subs.add(
      this.auth.sesion$.subscribe(() => {
        this.cdr.markForCheck();
      })
    );
  }

  /** Avisa al servidor que se cierra la sesión y redirige al inicio. */
  salir(): void {
    this.subs.add(
      this.auth.cerrarSesion().subscribe(() => {
        this.cdr.markForCheck();

        this.router.navigate(['/']).catch(() => {
          this.cdr.markForCheck();
        });
      })
    );
  }

  /** Libera las suscripciones del componente. */
  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
