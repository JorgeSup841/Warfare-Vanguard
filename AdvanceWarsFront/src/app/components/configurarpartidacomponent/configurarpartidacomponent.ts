import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { tableroservice } from '../../services/tableroservice';
import { partidaresumen } from '../../models/tablero';
import { faccion, comandante } from '../../models/partida';
import { forkJoin } from 'rxjs';

/** Muestra los datos de una partida nueva y permite iniciarla. */
@Component({
  selector: 'app-configurarpartidacomponent',
  standalone: false,
  templateUrl: './configurarpartidacomponent.html',
  styleUrl: './configurarpartidacomponent.css',
})
export class Configurarpartidacomponent implements OnInit {
  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private servicio = inject(tableroservice);
  private cdr = inject(ChangeDetectorRef);

  id = 0;
  partida: partidaresumen | null = null;
  iniciando = false;
  confirmar = false;
  mensaje = '';
  facciones: faccion[] = [];
  comandantes: comandante[] = [];
  seleccion: Record<number, { factionId: number; commanderId: number }> = {};
  guardando: number | null = null;
  mostrarElecciones = false;

  get seleccionesCompletas(): boolean {
    return Boolean(this.partida?.players.length) && (this.partida?.players ?? []).every(
      jugador => Boolean(jugador.factionCode) && Boolean(jugador.commanderCode)
    );
  }
  private readonly colores: Record<string, string> = { RED: '#d63031', BLUE: '#0984e3', GREEN: '#00b894', YELLOW: '#fdcb6e' };

  /** Ruta de retrato del paquete gráfico propio. */
  readonly retrato = (factionCode: string): string => `/assets/sprites/comandantes/${factionCode}.png`;

  /** Recupera el color visual de la facción seleccionada. */
  colorFaccion(code: string | null | undefined): string {
    return this.colores[code ?? ''] ?? '#879b90';
  }

  /** Filtra los comandantes que pertenecen a la facción elegida. */
  comandantesDe(id: number): comandante[] {
    const faction = this.facciones.find(f => f.id === id);
    return this.comandantes.filter(c => c.factionCode === faction?.code);
  }

  /** Actualiza las opciones locales al elegir otra facción. */
  cambiarFaccion(orden: number, value: string): void {
    const factionId = Number(value);
    const commanderId = this.comandantesDe(factionId)[0]?.id ?? 0;
    this.seleccion[orden] = { factionId, commanderId };
    this.cdr.markForCheck();
  }

  /** Actualiza la elección del comandante antes de guardar. */
  cambiarComandante(orden: number, value: string): void {
    if (this.seleccion[orden]) this.seleccion[orden].commanderId = Number(value);
  }

  /** La selección no se confirma hasta recibir el GameDTO actualizado. */
  guardarSeleccion(orden: number): void {
    const data = this.seleccion[orden];
    if (!data?.factionId || !data.commanderId || this.guardando !== null) return;
    this.guardando = orden;
    this.mensaje = '';
    this.servicio.seleccionarJugador(this.id, orden, data).subscribe({
      next: p => { this.partida = p; this.guardando = null; this.sincronizarSeleccion(); this.cdr.detectChanges(); },
      error: e => { this.mensaje = e.error?.message || 'No se pudo guardar la selección'; this.guardando = null; this.cdr.detectChanges(); },
    });
  }

  /** Reconcilia los selectores con las elecciones guardadas por Spring Boot. */
  private sincronizarSeleccion(): void {
    for (const p of this.partida?.players ?? []) {
      const faction = this.facciones.find(f => f.code === p.factionCode);
      const commander = this.comandantes.find(c => c.code === p.commanderCode);
      if (faction?.id && commander?.id) {
        this.seleccion[p.turnOrder] = { factionId: faction.id, commanderId: commander.id };
      }
    }
  }

  /** Lee el id de la ruta y carga la partida. */
  ngOnInit(): void {
    this.id = Number(this.ruta.snapshot.paramMap.get('id'));
    forkJoin({ facciones: this.servicio.getFacciones(), comandantes: this.servicio.getComandantes() })
      .subscribe({
        next: data => { this.facciones = data.facciones; this.comandantes = data.comandantes;
          this.sincronizarSeleccion(); this.cdr.detectChanges(); },
        error: e => { this.mensaje = e.error?.message || 'No se pudieron cargar facciones y comandantes';
          this.cdr.detectChanges(); },
      });
    this.cargar();
  }

  /** Pide al back los datos de la partida. */
  cargar(): void {
    this.servicio.getPartida(this.id).subscribe({
      next: (p) => {
        this.partida = p;
        this.sincronizarSeleccion();
        if (p.status === 'SETUP' && !this.seleccionesCompletas) this.mostrarElecciones = true;
        this.cdr.detectChanges();
      },
      error: (e) => {
        this.mensaje = e.error?.message || 'No se encontró la partida';
        this.cdr.detectChanges();
      },
    });
  }

  /** Muestra la pregunta de confirmación. */
  solicitarInicio(): void {
    this.confirmar = true;
  }

  /** Oculta la pregunta de confirmación. */
  cancelarInicio(): void {
    this.confirmar = false;
  }

  /** Pide al back iniciar la partida y abre el tablero. */
  confirmarInicio(): void {
    this.iniciando = true;
    this.confirmar = false;
    this.mensaje = '';
    this.servicio.iniciarPartida(this.id).subscribe({
      next: () => {
        this.iniciando = false;
        this.router.navigate(['/partidas', this.id, 'jugar']);
      },
      error: (e) => {
        this.iniciando = false;
        this.mensaje = e.error?.message || 'No se pudo iniciar la partida';
        this.cdr.detectChanges();
      },
    });
  }
}
