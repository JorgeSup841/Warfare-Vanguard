import { Component, Input } from '@angular/core';
import type { Juegocomponent } from '../juegocomponent/juegocomponent';

/** Panel de presentación: el juego conserva las acciones y el estado en el componente padre. */
@Component({
  selector: 'app-juego-historial',
  standalone: false,
  templateUrl: './juegohistorialcomponent.html',
})
export class Juegohistorialcomponent {
  @Input({ required: true }) juego!: Juegocomponent;
}
