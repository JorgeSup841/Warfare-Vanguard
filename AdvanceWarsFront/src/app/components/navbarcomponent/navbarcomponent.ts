import { Component, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { usuarioservice } from '../../services/usuarioservice';
@Component({
  selector: 'app-navbarcomponent',
  standalone: false,
  templateUrl: './navbarcomponent.html',
  styleUrl: './navbarcomponent.css',
})
export class Navbarcomponent {
  constructor(
    public auth: usuarioservice,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}
  salir(): void {
    this.auth.logout();
    void this.router.navigate(['/']);
    this.cdr.markForCheck();
  }
}
