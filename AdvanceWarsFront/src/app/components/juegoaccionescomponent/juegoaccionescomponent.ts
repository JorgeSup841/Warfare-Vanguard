import { Component, Input } from '@angular/core';
import type { Juegocomponent } from '../juegocomponent/juegocomponent';

/** Panel de presentación: el juego conserva las acciones y el estado en el componente padre. */
@Component({
  selector: 'app-juego-acciones',
  standalone: false,
  templateUrl: './juegoaccionescomponent.html',
})
export class Juegoaccionescomponent {
  @Input({ required: true }) juego!: Juegocomponent;
}
