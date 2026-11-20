import { TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Recuperarcomponent } from './recuperarcomponent';

describe('Recuperarcomponent', () => {
  it('se crea con su módulo Angular', async () => {
    TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule, RouterModule.forRoot([])],
      providers: [provideHttpClient(), provideHttpClientTesting()],
      declarations: [Recuperarcomponent],
    });
    // No se crea el canvas Phaser ni se envían solicitudes HTTP en esta prueba básica.
    TestBed.overrideComponent(Recuperarcomponent, { set: { template: '' } });
    await TestBed.compileComponents();
    const fixture = TestBed.createComponent(Recuperarcomponent);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
