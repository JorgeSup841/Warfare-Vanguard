import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { configuracionmapa } from '../../models/mapa';
import { juegoservice } from '../../services/juegoservice';
import { tableroservice } from '../../services/tableroservice';
/** Formulario para crear una partida en el servidor. */
@Component({
  selector: 'app-crearpartidacomponent',
  standalone: false,
  templateUrl: './crearpartidacomponent.html',
  styleUrl: './crearpartidacomponent.css',
})
export class Crearpartidacomponent implements OnInit, OnDestroy {
  nombres = ['Comandante Rojo', 'Comandante Azul'];
  cantidad = 2;
  ancho = 20;
  alto = 15;
  semilla = 12345;
  ocupado = false;
  error = '';
  config: configuracionmapa | null = null;
  private sub = new Subscription();
  constructor(
    private catalogs: juegoservice,
    private api: tableroservice,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}
  /** Inicializa y carga los datos de la pantalla. */
  ngOnInit(): void {
    this.sub.add(
      this.catalogs.getConfiguracionMapa().subscribe({
        next: (c) => {
          this.config = c;
          this.ancho = c.width;
          this.alto = c.height;
          this.cdr.markForCheck();
        },
        error: () => {
          this.error = 'No se pudo leer configuración local. Introduce dimensiones manualmente.';
          this.cdr.markForCheck();
        },
      }),
    );
  }
  /** Actualiza la lista de jugadores de la partida. */
  cambiarCantidad(): void {
    this.nombres = Array.from(
      { length: this.cantidad },
      (_, i) => this.nombres[i] || `Jugador ${i + 1}`,
    );
    this.cdr.markForCheck();
  }
  /** Valida los jugadores y crea una partida en Spring Boot. */
  crear(): void {
    if (this.ocupado) return;
    const nombres = this.nombres.map((n) => n.trim());
    if (
      nombres.length < 2 ||
      nombres.length > 4 ||
      nombres.some((n) => !n) ||
      new Set(nombres.map((n) => n.toLowerCase())).size !== nombres.length
    ) {
      this.error = 'Usa entre 2 y 4 nombres diferentes, sin campos vacíos.';
      this.cdr.markForCheck();
      return;
    }
    if (
      ![this.ancho, this.alto, this.semilla].every(Number.isSafeInteger) ||
      this.ancho < 8 ||
      this.alto < 8
    ) {
      this.error = 'Verifica dimensiones y semilla numéricas.';
      this.cdr.markForCheck();
      return;
    }
    this.ocupado = true;
    this.error = '';
    this.cdr.markForCheck();
    this.sub.add(
      this.api
        .crearPartida({
          playerNames: nombres,
          width: this.ancho,
          height: this.alto,
          seed: this.semilla,
        })
        .subscribe({
          next: (p) => {
            this.ocupado = false;
            this.cdr.markForCheck();
            this.router.navigate(['/partidas', p.id, 'configurar']).catch(() => { this.error = 'No se pudo abrir la pantalla solicitada.'; this.cdr.markForCheck(); });
          },
          error: (e) => {
            this.ocupado = false;
            this.error = `No fue posible crear la partida (HTTP ${e.status || 0}). Revisa POST /api/games.`;
            this.cdr.markForCheck();
          },
        }),
    );
  }
  /** Libera los recursos y suscripciones del componente. */
  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
}
