import { Component } from '@angular/core';
import { Title } from '@angular/platform-browser';
/** Componente raíz de la aplicación. */
@Component({
  selector: 'app-root',
  standalone: false,
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  readonly titulo = 'AURA WARS';

  constructor(title: Title) {
    title.setTitle(this.titulo);
  }
}
