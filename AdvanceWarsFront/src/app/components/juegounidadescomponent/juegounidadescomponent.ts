import { Component, Input } from '@angular/core';
import type { Juegocomponent } from '../juegocomponent/juegocomponent';

/** Panel de presentación: el juego conserva las acciones y el estado en el componente padre. */
@Component({
  selector: 'app-juego-unidades',
  standalone: false,
  templateUrl: './juegounidadescomponent.html',
})
export class Juegounidadescomponent {
  @Input({ required: true }) juego!: Juegocomponent;
}
