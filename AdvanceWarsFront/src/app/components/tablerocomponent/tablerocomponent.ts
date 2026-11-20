import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { forkJoin, Subscription } from 'rxjs';
import { casilla, configuracionmapa, terreno } from '../../models/mapa';
import { faccion, partida, propiedad, propiedadjuego } from '../../models/partida';
import { unidad, unidadjuego } from '../../models/unidad';
import { juegoservice } from '../../services/juegoservice';
import { demopartidaservice } from '../../services/demopartidaservice';

/** Gestiona la vista interactiva del tablero de demostración. */
@Component({selector:'app-tablerocomponent',standalone:false,templateUrl:'./tablerocomponent.html',styleUrl:'./tablerocomponent.css'})
export class Tablerocomponent implements OnInit, OnDestroy {
  cargando = true;
  error = '';
  cantidadJugadores:2|3|4 = 2;
  juego: partida|null = null;
  seleccion:casilla|null = null;
  configuracion:configuracionmapa|null=null;
  terrenos:terreno[]=[];
  facciones:faccion[]=[];
  unidades:unidad[]=[];
  propiedades:propiedad[]=[];
  private peticion?:Subscription;
  constructor(private catalogos:juegoservice,private demos:demopartidaservice,private cdr:ChangeDetectorRef){}
  /** Inicializa y carga los datos de la pantalla. */
  ngOnInit():void {this.cargarDatos();}
  /** Consulta los datos complementarios para actualizar la vista. */
  cargarDatos():void {
    this.peticion?.unsubscribe();
    this.cargando=true;this.error='';this.juego=null;this.seleccion=null;this.cdr.markForCheck();
    this.peticion=forkJoin({
      config:this.catalogos.getConfiguracionMapa(), terrenos:this.catalogos.getTerrenos(),
      facciones:this.catalogos.getFacciones(), unidades:this.catalogos.getUnidades(), propiedades:this.catalogos.getPropiedades()
    }).subscribe({
      next:r=>{
        this.configuracion=r.config;this.terrenos=r.terrenos;this.facciones=r.facciones;
        this.unidades=r.unidades;this.propiedades=r.propiedades;
        this.cargando=false;
        this.cambiarJugadores(this.cantidadJugadores);
        this.cdr.markForCheck();
      },
      error:e=>{this.cargando=false;this.error=`No se pudieron cargar los catálogos (HTTP ${e.status||0}).`;this.cdr.markForCheck();}
    });
  }
  /** Modifica la cantidad de jugadores de la demostración. */
  cambiarJugadores(cantidad:2|3|4):void {
    if(!this.configuracion)return;
    try{
      const nueva=this.demos.crearDemo(cantidad,this.configuracion,this.facciones,this.unidades);
      this.cantidadJugadores=cantidad;this.juego=nueva;this.seleccion=null;this.error='';
    }catch(e){this.error=e instanceof Error?e.message:'Error preparando la demo.';this.juego=null;}
    this.cdr.markForCheck();
  }
  /** Actualiza la casilla seleccionada en el tablero. */
  seleccionarCasilla(c:casilla|null):void {this.seleccion=c;this.cdr.markForCheck();}
  get unidadActual():unidadjuego|null {return this.juego?.units.find(u=>u.x===this.seleccion?.x&&u.y===this.seleccion?.y)??null;}
  get propiedadActual():propiedadjuego|null {return this.juego?.properties.find(p=>p.x===this.seleccion?.x&&p.y===this.seleccion?.y)??null;}
  /** Resuelve el nombre visible del tipo de terreno. */
  nombreTerreno(codigo:string):string{return this.terrenos.find(t=>t.terrainType===codigo)?.name??codigo;}
  /** Resuelve el nombre visible del tipo de unidad. */
  nombreUnidad(codigo:string):string{return this.unidades.find(u=>u.unitType===codigo)?.name??codigo;}
  /** Resuelve el nombre visible del tipo de propiedad. */
  nombrePropiedad(codigo:string):string{return this.propiedades.find(p=>p.type===codigo)?.name??codigo;}
  /** Resuelve el nombre visible del jugador. */
  nombreJugador(orden:number|null):string{return orden===null?'Neutral':this.juego?.players.find(j=>j.turnOrder===orden)?.name??'Desconocido';}
  /** Obtiene el color asociado a la facción del jugador. */
  colorJugador(codigoFaccion:string):string{return this.facciones.find(f=>f.code===codigoFaccion)?.color??'#ffffff';}
  /** Libera los recursos y suscripciones del componente. */
  ngOnDestroy():void {this.peticion?.unsubscribe();}
}
