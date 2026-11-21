import { Component, ChangeDetectorRef, OnInit, OnDestroy, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription, forkJoin } from 'rxjs';
import { adminservice } from '../../services/adminservice';
import { usuarioservice } from '../../services/usuarioservice';
import { ReporteService } from '../../services/reporte.service';
import { usuario, rolusuario, registro } from '../../models/usuario';
import {
  resumenadmin, partidaadmin, mapaadmin, tropaadmin,
  registrobatallaadmin, sesionadmin, actividadadmin
} from '../../models/admin';

type seccionadmin = 'resumen' | 'usuarios' | 'partidas' | 'sesiones' | 'actividad';
type confirmacionadmin =
  | { tipo: 'rol'; usuario: usuario; valor: rolusuario; texto: string }
  | { tipo: 'estado'; usuario: usuario; valor: boolean; texto: string }
  | { tipo: 'partida'; id: number; texto: string }
  | { tipo: 'sesion'; id: string; texto: string };

/** Consola administrativa conectada al backend: usuarios, partidas, sesiones y actividad. */
@Component({
  selector: 'app-admincomponent',
  standalone: false,
  templateUrl: './admincomponent.html',
  styleUrl: './admincomponent.css'
})
export class Admincomponent implements OnInit, OnDestroy {
  private readonly admin = inject(adminservice);
  readonly auth = inject(usuarioservice);
  private readonly pdf = inject(ReporteService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly subs = new Subscription();

  seccion: seccionadmin = 'resumen';
  procesando = false;
  mensaje = '';
  error = '';

  resumen: resumenadmin | null = null;
  usuarios: usuario[] = [];
  busqueda = '';
  idConsulta = 0;
  detalleUsuario: usuario | null = null;
  nuevo: registro = { username: '', email: '', password: '' };

  partidas: partidaadmin[] = [];
  partida: partidaadmin | null = null;
  mapa: mapaadmin | null = null;
  tropas: tropaadmin[] = [];
  historial: registrobatallaadmin[] = [];
  idPDF = 0;

  sesiones: sesionadmin[] = [];
  soloActivas = true;
  actividad: actividadadmin[] = [];
  idActividad = 0;
  confirmacion: confirmacionadmin | null = null;

  /** Recupera las estadísticas administrativas al abrir la pantalla. */
  ngOnInit(): void {
    this.cargarResumen();
  }

  /** Abre una sección y obtiene la información que corresponde. */
  abrir(seccion: seccionadmin): void {
    this.seccion = seccion;
    this.mensaje = '';
    this.error = '';
    this.confirmacion = null;
    if (seccion === 'resumen') this.cargarResumen();
    if (seccion === 'usuarios') this.cargarUsuarios();
    if (seccion === 'partidas') this.cargarPartidas();
    if (seccion === 'sesiones') this.cargarSesiones();
    if (seccion === 'actividad') this.cargarActividad();
    this.cdr.markForCheck();
  }

  /** Convierte una respuesta HTTP en un mensaje legible. */
  private fallo(e: HttpErrorResponse, accion: string): void {
    this.procesando = false;
    this.error = typeof e.error?.message === 'string' ? e.error.message : `${accion} (HTTP ${e.status || 0}).`;
    this.cdr.detectChanges();
  }

  /** Consulta los indicadores de administración. */
  cargarResumen(): void {
    this.procesando = true;
    this.subs.add(this.admin.resumen().subscribe({
      next: datos => { this.resumen = datos; this.procesando = false; this.cdr.detectChanges(); },
      error: e => this.fallo(e, 'No se pudo cargar el resumen')
    }));
  }

  /** Consulta todas las cuentas. */
  cargarUsuarios(): void {
    this.procesando = true;
    this.subs.add(this.admin.listarUsuarios().subscribe({
      next: datos => { this.usuarios = datos; this.procesando = false; this.cdr.detectChanges(); },
      error: e => this.fallo(e, 'No se pudieron consultar usuarios')
    }));
  }

  /** Filtra por nombre, correo o ID sin hacer peticiones extras. */
  get usuariosFiltrados(): usuario[] {
    const filtro = this.busqueda.trim().toLowerCase();
    return filtro ? this.usuarios.filter(u =>
      `${u.id} ${u.username} ${u.email || ''}`.toLowerCase().includes(filtro)) : this.usuarios;
  }

  /** Consulta un usuario por ID desde el backend. */
  consultarUsuario(): void {
    if (!Number.isSafeInteger(this.idConsulta) || this.idConsulta <= 0) {
      this.error = 'Escribe un ID de usuario válido.';
      return;
    }
    this.error = '';
    this.procesando = true;
    this.subs.add(this.admin.obtenerUsuario(this.idConsulta).subscribe({
      next: datos => { this.detalleUsuario = datos; this.procesando = false; this.cdr.detectChanges(); },
      error: e => this.fallo(e, 'No se encontró el usuario')
    }));
  }

  /** Registra una cuenta USER; su correo requiere verificación OTP posterior. */
  crearUsuario(): void {
    const nombre = this.nuevo.username.trim();
    const correo = this.nuevo.email.trim();
    if (nombre.length < 3 || nombre.length > 50 || !correo.includes('@') ||
      this.nuevo.password.length < 12 || this.nuevo.password.length > 64) {
      this.error = 'Verifica usuario (3-50), correo y contraseña (12-64 caracteres).';
      return;
    }
    this.procesando = true;
    this.error = '';
    this.mensaje = '';
    this.subs.add(this.admin.crearUsuario({ username: nombre, email: correo, password: this.nuevo.password }).subscribe({
      next: () => {
        this.nuevo = { username: '', email: '', password: '' };
        this.procesando = false;
        this.mensaje = 'Cuenta USER registrada. El dueño debe verificar el código OTP recibido por correo.';
        this.cargarUsuarios();
        this.cdr.detectChanges();
      },
      error: e => this.fallo(e, 'No se pudo registrar la cuenta')
    }));
  }

  /** Evita alterar los permisos de la cuenta actual. */
  esMiCuenta(u: usuario): boolean {
    return u.id != null && u.id === this.auth.getUsuarioActual()?.id;
  }

  /** Pide confirmación antes de cambiar el rol de otra cuenta. */
  cambiarRol(u: usuario, rol: rolusuario): void {
    if (this.procesando || this.esMiCuenta(u) || u.id == null || u.role === rol) return;
    this.confirmacion = { tipo: 'rol', usuario: u, valor: rol,
      texto: `¿Cambiar el rol de ${u.username} a ${rol}?` };
  }

  /** Pide confirmación antes de cambiar el estado de otra cuenta. */
  cambiarEstado(u: usuario, habilitado: boolean): void {
    if (this.procesando || this.esMiCuenta(u) || u.id == null || u.enabled === habilitado) return;
    if (habilitado && !u.emailVerified) {
      this.error = 'No se puede habilitar una cuenta sin verificar su correo.';
      return;
    }
    this.confirmacion = { tipo: 'estado', usuario: u, valor: habilitado,
      texto: `¿${habilitado ? 'Habilitar' : 'Deshabilitar'} a ${u.username}?` };
  }

  /** Solicita confirmación de eliminación de partida. */
  prepararEliminarPartida(id: number): void {
    this.confirmacion = { tipo: 'partida', id,
      texto: `¿Eliminar definitivamente la partida #${id} y su historial? Esta acción no se puede deshacer.` };
  }

  /** Solicita confirmación de cierre de sesión remoto. */
  prepararRevocacion(id: string): void {
    this.confirmacion = { tipo: 'sesion', id,
      texto: '¿Revocar esta sesión? El usuario tendrá que iniciar sesión nuevamente.' };
  }

  /** Descarta la acción administrativa pendiente. */
  cancelarCambio(): void {
    this.confirmacion = null;
  }

  /** Ejecuta una acción únicamente después de la confirmación. */
  confirmarCambio(): void {
    const c = this.confirmacion;
    if (!c || this.procesando) return;
    this.confirmacion = null;
    this.procesando = true;
    this.mensaje = '';
    this.error = '';

    if (c.tipo === 'rol' || c.tipo === 'estado') {
      const id = c.usuario.id;
      if (id == null) { this.procesando = false; return; }
      const accion = c.tipo === 'rol'
        ? this.admin.cambiarRol(id, c.valor)
        : this.admin.cambiarEstado(id, c.valor);
      this.subs.add(accion.subscribe({
        next: usuarioActualizado => {
          this.usuarios = this.usuarios.map(u => u.id === id ? usuarioActualizado : u);
          if (this.detalleUsuario?.id === id) this.detalleUsuario = usuarioActualizado;
          this.procesando = false;
          this.mensaje = 'Usuario actualizado correctamente.';
          this.cdr.detectChanges();
        },
        error: e => this.fallo(e, 'No se pudo actualizar el usuario')
      }));
    } else if (c.tipo === 'partida') {
      this.subs.add(this.admin.eliminarPartida(c.id).subscribe({
        next: () => {
          this.partidas = this.partidas.filter(p => p.game.id !== c.id);
          if (this.partida?.game.id === c.id) { this.partida = null; this.mapa = null; this.tropas = []; this.historial = []; }
          this.procesando = false;
          this.mensaje = 'Partida eliminada.';
          this.cdr.detectChanges();
        },
        error: e => this.fallo(e, 'No se pudo eliminar la partida')
      }));
    } else {
      this.subs.add(this.admin.revocarSesion(c.id).subscribe({
        next: () => {
          this.procesando = false;
          this.mensaje = 'Sesión revocada.';
          this.cargarSesiones();
          this.cdr.detectChanges();
        },
        error: e => this.fallo(e, 'No se pudo revocar la sesión')
      }));
    }
  }

  /** Lista las partidas existentes, sin limitarse al dueño actual. */
  cargarPartidas(): void {
    this.procesando = true;
    this.subs.add(this.admin.listarPartidas().subscribe({
      next: datos => { this.partidas = datos; this.procesando = false; this.cdr.detectChanges(); },
      error: e => this.fallo(e, 'No se pudieron listar las partidas')
    }));
  }

  /** Obtiene simultáneamente el detalle, mapa, unidades e historial de una partida. */
  verPartida(id: number): void {
    this.procesando = true;
    this.error = '';
    this.partida = null;
    this.mapa = null;
    this.tropas = [];
    this.historial = [];
    this.subs.add(forkJoin({
      partida: this.admin.obtenerPartida(id),
      mapa: this.admin.obtenerMapa(id),
      tropas: this.admin.obtenerTropas(id),
      historial: this.admin.obtenerHistorial(id)
    }).subscribe({
      next: datos => {
        this.partida = datos.partida;
        this.mapa = datos.mapa;
        this.tropas = datos.tropas;
        this.historial = datos.historial;
        this.idPDF = id;
        this.procesando = false;
        this.cdr.detectChanges();
      },
      error: e => this.fallo(e, 'No se pudo consultar la partida completa')
    }));
  }

  /** Descarga el informe de la partida como ADMIN. */
  pdfPartida(): void {
    if (!Number.isSafeInteger(this.idPDF) || this.idPDF <= 0) {
      this.error = 'Escribe un ID de partida válido.';
      return;
    }
    this.procesando = true;
    this.error = '';
    this.subs.add(this.pdf.obtenerComoAdmin(this.idPDF).subscribe({
      next: blob => {
        try {
          this.pdf.guardarArchivo(blob, this.idPDF);
          this.mensaje = 'PDF descargado.';
        } catch (error) {
          this.error = error instanceof Error ? error.message : 'El PDF no es válido.';
        }
        this.procesando = false;
        this.cdr.detectChanges();
      },
      error: e => this.fallo(e, 'No se pudo obtener el PDF')
    }));
  }

  /** Consulta sesiones activas o todas según filtro. */
  cargarSesiones(): void {
    this.procesando = true;
    const consulta = this.soloActivas ? this.admin.listarSesionesActivas() : this.admin.listarSesiones();
    this.subs.add(consulta.subscribe({
      next: datos => { this.sesiones = datos; this.procesando = false; this.cdr.detectChanges(); },
      error: e => this.fallo(e, 'No se pudieron consultar sesiones')
    }));
  }

  /** Consulta actividad global o de un usuario concreto. */
  cargarActividad(): void {
    if (this.idActividad && (!Number.isSafeInteger(this.idActividad) || this.idActividad < 0)) {
      this.error = 'ID de usuario inválido.';
      return;
    }
    this.procesando = true;
    const consulta = this.idActividad > 0
      ? this.admin.actividadUsuario(this.idActividad) : this.admin.listarActividad();
    this.subs.add(consulta.subscribe({
      next: datos => { this.actividad = datos; this.procesando = false; this.cdr.detectChanges(); },
      error: e => this.fallo(e, 'No se pudo consultar actividad')
    }));
  }

  /** Cancela suscripciones al abandonar la pantalla. */
  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }
}
