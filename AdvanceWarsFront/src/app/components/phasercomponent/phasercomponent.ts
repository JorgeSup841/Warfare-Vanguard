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
import type { EscenaMapa } from '../../phaser/escenamapa';

@Component({
  selector: 'app-phasercomponent',
  standalone: false,
  templateUrl: './phasercomponent.html',
  styleUrl: './phasercomponent.css',
})
export class Phasercomponent implements AfterViewInit, OnChanges, OnDestroy {
  @ViewChild('contenedor', { static: true }) contenedor!: ElementRef<HTMLDivElement>;
  @Input() juego: partida | null = null;
  @Input() terrenos: terreno[] = [];
  @Input() facciones: faccion[] = [];
  @Input() seleccion: casilla | null = null;
  @Output() seleccionCambio = new EventEmitter<casilla | null>();
  error = '';
  private phaser: Phaser.Game | null = null;
  private escena: EscenaMapa | null = null;
  private vistaLista = false;
  private destruido = false;
  private iniciando = false;
  constructor(
    private zone: NgZone,
    private cdr: ChangeDetectorRef,
  ) {}
  ngAfterViewInit(): void {
    this.vistaLista = true;
    void this.actualizar();
  }
  ngOnChanges(_changes: SimpleChanges): void {
    if (this.vistaLista) void this.actualizar();
  }
  private async actualizar(): Promise<void> {
    if (this.destruido || !this.vistaLista || !this.juego || this.iniciando) return;
    this.iniciando = true;
    try {
      const [PhaserLib, { EscenaMapa, TAM_CASILLA }] = await Promise.all([
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
          const element = this.contenedor.nativeElement;
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
  ngOnDestroy(): void {
    this.destruido = true;
    this.zone.runOutsideAngular(() => this.phaser?.destroy(true));
    this.phaser = null;
    this.escena = null;
  }
}
