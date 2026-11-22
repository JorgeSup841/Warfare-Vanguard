import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { tableroservice } from '../../services/tableroservice';

/** Formulario para crear una partida nueva. */
@Component({
  selector: 'app-crearpartidacomponent',
  standalone: false,
  templateUrl: './crearpartidacomponent.html',
  styleUrl: './crearpartidacomponent.css',
})
export class Crearpartidacomponent {
  private servicio = inject(tableroservice);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  cantidad = 2;
  nombres = ['Comandante Rojo', 'Comandante Azul'];
  ancho = 20;
  alto = 15;
  semilla: number | null = null;
  creando = false;
  mensaje = '';

  porIndice = (i: number) => i;

  /** Ajusta la lista de nombres según la cantidad de jugadores. */
  cambiarCantidad(): void {
    const nuevos: string[] = [];
    for (let i = 0; i < this.cantidad; i++) {
      nuevos.push(this.nombres[i] || `Jugador ${i + 1}`);
    }
    this.nombres = nuevos;
    this.cdr.markForCheck();
  }

  /** Pone una semilla aleatoria. */
  semillaAlAzar(): void {
    this.semilla = Math.floor(Math.random() * 1000000);
    this.cdr.markForCheck();
  }

  /** Revisa los datos y pide al back crear la partida. */
  crear(): void {
    const nombres = this.nombres.map((n) => n.trim());

    if (nombres.some((n) => n === '')) {
      this.mensaje = 'Ningún jugador puede quedar sin nombre';
      this.cdr.markForCheck();
      return;
    }
    if (this.ancho < 5 || this.ancho > 50 || this.alto < 5 || this.alto > 50) {
      this.mensaje = 'El mapa debe medir entre 5 y 50 casillas por lado';
      this.cdr.markForCheck();
      return;
    }

    this.creando = true;
    this.mensaje = '';
    this.cdr.markForCheck();

    const datos = {
      playerNames: nombres,
      width: this.ancho,
      height: this.alto,
      seed: this.semilla,
    };

    this.servicio.crearPartida(datos).subscribe({
      next: (p) => {
        this.creando = false;
        this.cdr.markForCheck();
        this.router.navigate(['/partidas', p.id, 'configurar']).catch(() => {
          this.mensaje = 'No se pudo abrir la configuración de la partida.';
          this.cdr.markForCheck();
        });
      },
      error: (e) => {
        this.creando = false;
        this.mensaje = e.error?.message || 'No se pudo crear la partida';
        this.cdr.detectChanges();
      },
    });
  }
}
