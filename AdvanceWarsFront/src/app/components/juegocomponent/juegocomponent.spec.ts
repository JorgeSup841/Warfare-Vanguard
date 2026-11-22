import { CommonModule } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, RouterModule } from '@angular/router';
import { Juegocomponent } from './juegocomponent';

describe('Juegocomponent - tablero de prueba', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule, RouterModule.forRoot([])],
      declarations: [Juegocomponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { data: { demo: true }, paramMap: convertToParamMap({}) } },
        },
      ],
    })
      // Probamos la activación del modo demo sin cargar Phaser ni pedir catálogos.
      .overrideComponent(Juegocomponent, { set: { template: '' } })
      .compileComponents();
  });

  it('habilita el modo demo sin un identificador de partida en el backend', () => {
    const fixture = TestBed.createComponent(Juegocomponent);
    expect(fixture.componentInstance.modoDemo).toBe(true);
    expect(fixture.componentInstance.id).toBe(0);
    fixture.destroy();
  });
});
