import { CommonModule } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { SimpleChange } from '@angular/core';
import { Phasercomponent } from './phasercomponent';

describe('Phasercomponent - prueba de plantilla y CDR', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [Phasercomponent],
    }).compileComponents();
  });

  it('carga la plantilla HTML real y encuentra el contenedor Phaser', () => {
    const fixture = TestBed.createComponent(Phasercomponent);
    // El juego es null: no se intenta iniciar WebGL durante esta prueba.
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.contenedor-phaser')).toBeTruthy();
    fixture.destroy();
  });

  it('actualiza el mensaje de error en la plantilla', () => {
    const fixture = TestBed.createComponent(Phasercomponent);
    fixture.detectChanges();
    fixture.componentInstance.error = 'Error de prueba';
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent).toContain('Error de prueba');
    fixture.destroy();
  });

  it('no reinicia la escena si cambia solamente la selección', () => {
    const fixture = TestBed.createComponent(Phasercomponent);
    const componente = fixture.componentInstance;
    fixture.detectChanges();
    // No hay juego: ngOnChanges tampoco debe provocar un nuevo inicio.
    componente.ngOnChanges({ seleccion: new SimpleChange(null, { x: 1, y: 1 }, false) });
    expect(componente.error).toBe('');
    fixture.destroy();
  });
});
