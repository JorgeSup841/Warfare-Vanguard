import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  NgZone,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import type Phaser from 'phaser';
import { casilla, terreno } from '../../models/mapa';
import { faccion, partida } from '../../models/partida';

/** Integra una escena Phaser con los datos de Angular. */
@Component({
  selector: 'app-phasercomponent',
  standalone: false,
  templateUrl: './phasercomponent.html',
  styleUrl: './phasercomponent.css',
})
export class Phasercomponent implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('contenedor', { static: true }) contenedor?: ElementRef<HTMLDivElement>;
  @Input() juego: partida | null = null;
  @Input() terrenos: terreno[] = [];
  @Input() facciones: faccion[] = [];
  @Input() seleccion: casilla | null = null;
  @Output() readonly seleccionCambio = new EventEmitter<casilla | null>();
  error = '';
  private phaser: Phaser.Game | null = null;
  private vistaLista = false;
  private destruido = false;
  private iniciando = false;
  constructor(
    private zone: NgZone,
    private cdr: ChangeDetectorRef,
  ) {}
  /** Inicializa los recursos gráficos al estar disponible la vista. */
  ngAfterViewInit(): void {
    this.vistaLista = true;
    this.actualizar();
  }
  /** Actualiza la escena cuando cambian las entradas del componente. */
  ngOnChanges(_changes: SimpleChanges): void {
    if (this.vistaLista) this.actualizar();
  }
  /** Actualiza los objetos y datos de la escena Phaser. */
  private async actualizar(): Promise<void> {
    if (this.destruido || !this.vistaLista || !this.juego || this.iniciando) return;
    this.iniciando = true;
    try {
      const [PhaserLib, { EscenaMapa }] = await Promise.all([
        import('phaser'),
        import('../../phaser/escenamapa'),
      ]);
      if (this.destruido || !this.juego) return;
      const mapa = this.juego.map;
      const bases = this.juego.properties
        .filter((p) => p.type === 'HEADQUARTERS')
        .sort((a, b) => (a.ownerOrder ?? 0) - (b.ownerOrder ?? 0))
        .map((p) => ({ x: p.x, y: p.y }));
      const datos = {
        mapa,
        terrenos: this.terrenos,
        bases,
        alPasarSobre: (c: casilla | null) => {
          if (!this.destruido)
            this.zone.run(() => {
              this.seleccionCambio.emit(c);
              this.cdr.markForCheck();
            });
        },
      };
      this.zone.runOutsideAngular(() => {
        if (!this.phaser) {
          const element = this.contenedor?.nativeElement;
          if (!element) throw new Error('No se encontró el contenedor de Phaser.');
          const ancho = Math.max(320, element.clientWidth || 800);
          const alto = Math.max(300, element.clientHeight || 500);
          this.phaser = new PhaserLib.Game({
            type: PhaserLib.AUTO,
            parent: element,
            width: ancho,
            height: alto,
            backgroundColor: '#14201a',
            scale: { mode: PhaserLib.Scale.RESIZE, width: ancho, height: alto },
            scene: [],
          });
          this.phaser.scene.add('EscenaMapa', EscenaMapa, true, datos);
        } else {
          const activa = this.phaser.scene.getScene('EscenaMapa');
          activa.scene.restart(datos);
        }
      });
      this.error = '';
      this.cdr.markForCheck();
    } catch (error) {
      if (!this.destruido) {
        console.error('Error Phaser demo', error);
        this.error = 'No se pudo iniciar el tablero de prueba.';
        this.cdr.markForCheck();
      }
    } finally {
      this.iniciando = false;
    }
  }
  /** Libera los recursos y suscripciones del componente. */
  ngOnDestroy(): void {
    this.destruido = true;
    this.zone.runOutsideAngular(() => this.phaser?.destroy(true));
    this.phaser = null;
  }
}
