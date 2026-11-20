import { TestBed } from '@angular/core/testing';
import { demopartidaservice } from './demopartidaservice';
import { configuracionmapa } from '../models/mapa';
import { faccion } from '../models/partida';

describe('demopartidaservice', () => {
  it('crea las casillas y jugadores para dos participantes', () => {
    TestBed.configureTestingModule({});
    const service=TestBed.inject(demopartidaservice);
    const config={width:20,height:15} as configuracionmapa;
    const facciones=[{code:'R',name:'Rojo'},{code:'A',name:'Azul'}] as faccion[];
    const p=service.crearDemo(2,config,facciones,[]);
    expect(p.map.tiles.length).toBe(300);
    expect(p.players.length).toBe(2);
    expect(p.units.length).toBe(4);
    expect(p.properties.length).toBe(4);
  });
});
