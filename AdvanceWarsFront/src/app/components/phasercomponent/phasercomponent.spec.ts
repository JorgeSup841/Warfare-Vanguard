import { TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Phasercomponent } from './phasercomponent';

describe('Phasercomponent', () => {
  it('se crea con su módulo Angular', async () => {
    TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule, RouterModule.forRoot([])],
      providers: [provideHttpClient(), provideHttpClientTesting()],
      declarations: [Phasercomponent],
    });
    // No se crea el canvas Phaser ni se envían solicitudes HTTP en esta prueba básica.
    TestBed.overrideComponent(Phasercomponent, { set: { template: '' } });
    await TestBed.compileComponents();
    const fixture = TestBed.createComponent(Phasercomponent);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
