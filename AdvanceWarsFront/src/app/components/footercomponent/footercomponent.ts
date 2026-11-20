import { Component } from '@angular/core';
/** Pie de página con la información del proyecto. */
@Component({
  selector: 'app-footercomponent',
  standalone: false,
  templateUrl: './footercomponent.html',
  styleUrl: './footercomponent.css',
})
export class Footercomponent {
  readonly anioActual = new Date().getFullYear();
}
