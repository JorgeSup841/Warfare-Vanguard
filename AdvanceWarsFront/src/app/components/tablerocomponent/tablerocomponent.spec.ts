import { TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Tablerocomponent } from './tablerocomponent';

describe('Tablerocomponent', () => {
  it('se crea con su módulo Angular', async () => {
    TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule, RouterModule.forRoot([])],
      providers: [provideHttpClient(), provideHttpClientTesting()],
      declarations: [Tablerocomponent],
    });
    // No se crea el canvas Phaser ni se envían solicitudes HTTP en esta prueba básica.
    TestBed.overrideComponent(Tablerocomponent, { set: { template: '' } });
    await TestBed.compileComponents();
    const fixture = TestBed.createComponent(Tablerocomponent);
    expect(fixture.componentInstance).toBeTruthy();
  });
});
