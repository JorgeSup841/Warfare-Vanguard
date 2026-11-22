import { ChangeDetectorRef, Component, Input, inject } from '@angular/core';
import type { Juegocomponent } from '../juegocomponent/juegocomponent';

/** Panel de presentación: el juego conserva las acciones y el estado en el componente padre. */
@Component({
  selector: 'app-juego-unidades',
  standalone: false,
  templateUrl: './juegounidadescomponent.html',
})
export class Juegounidadescomponent {
  @Input({ required: true }) juego!: Juegocomponent;
  private readonly cdr = inject(ChangeDetectorRef);

  /** Refresca la ficha aun cuando el objeto `juego` no cambie de referencia. */
  marcarCambio(): void {
    this.cdr.markForCheck();
  }
}
