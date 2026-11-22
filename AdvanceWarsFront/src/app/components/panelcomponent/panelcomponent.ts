import { Component, ChangeDetectorRef, OnInit, OnDestroy, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { usuarioservice } from '../../services/usuarioservice';
import { usuarioautenticado } from '../../models/usuario';

/** Muestra los datos del usuario autenticado en el panel. */
@Component({
  selector: 'app-panelcomponent',
  standalone: false,
  templateUrl: './panelcomponent.html',
  styleUrl: './panelcomponent.css'
})
export class Panelcomponent implements OnInit, OnDestroy {

  readonly auth = inject(usuarioservice);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly subs = new Subscription();

  perfil: usuarioautenticado | null = null;
  cargando = false;
  error = '';

  /** Consulta los datos del perfil al abrir el panel. */
  ngOnInit(): void {
    this.cargarPerfil();
  }

  /** Solicita el perfil al backend. */
  cargarPerfil(): void {
    if (this.cargando) return;

    this.cargando = true;
    this.error = '';
    this.cdr.markForCheck();

    this.subs.add(
      this.auth.getPerfil().subscribe({
        next: (respuesta) => {
          this.perfil = respuesta;
          this.cargando = false;
          this.cdr.detectChanges();
        },
        error: (e: HttpErrorResponse) => {
          this.cargando = false;

          if (e.status === 401) {
            this.auth.logout();
            this.error = 'Tu sesión expiró. Vuelve a iniciar sesión.';
          } else if (e.status === 0) {
            this.error = 'No hay conexión con el backend.';
          } else {
            this.error = e.error?.message || 'No se pudo cargar el perfil.';
          }

          this.cdr.detectChanges();
        }
      })
    );
  }

  /** Finaliza las suscripciones al cerrar el componente. */
  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
