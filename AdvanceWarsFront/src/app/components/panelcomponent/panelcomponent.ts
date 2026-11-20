import { Component, ChangeDetectorRef } from '@angular/core';
import { usuarioservice } from '../../services/usuarioservice';
/** Pantalla principal de la sesión autenticada. */
@Component({
  selector: 'app-panelcomponent',
  standalone: false,
  templateUrl: './panelcomponent.html',
  styleUrl: './panelcomponent.css',
})
export class Panelcomponent {
  constructor(
    public auth: usuarioservice,
    private cdr: ChangeDetectorRef,
  ) {}
}
