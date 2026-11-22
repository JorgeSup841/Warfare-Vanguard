import { ChangeDetectorRef, Component, Input, inject } from '@angular/core';
import type { Juegocomponent } from '../juegocomponent/juegocomponent';

/** Panel de presentación: el juego conserva las acciones y el estado en el componente padre. */
@Component({
  selector: 'app-juego-acciones',
  standalone: false,
  templateUrl: './juegoaccionescomponent.html',
})
export class Juegoaccionescomponent {
  @Input({ required: true }) juego!: Juegocomponent;
  private readonly cdr = inject(ChangeDetectorRef);

  /** Refresca la ficha aun cuando el objeto `juego` no cambie de referencia. */
  marcarCambio(): void {
    this.cdr.markForCheck();
  }
}
