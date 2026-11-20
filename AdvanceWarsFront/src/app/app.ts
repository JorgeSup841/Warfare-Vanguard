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
  constructor(title: Title) {
    title.setTitle('AURA WARS');
  }
}
