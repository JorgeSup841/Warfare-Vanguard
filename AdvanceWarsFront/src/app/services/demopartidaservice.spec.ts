import { TestBed } from '@angular/core/testing';
import { demopartidaservice } from './demopartidaservice';
import { configuracionmapa } from '../models/mapa';
import { faccion } from '../models/partida';

describe('demopartidaservice', () => {
  it('crea las casillas y jugadores para dos participantes', () => {
    TestBed.configureTestingModule({});
    const service = TestBed.inject(demopartidaservice);
    const config = { width: 20, height: 15 } as configuracionmapa;
    const facciones = [
      { code: 'R', name: 'Rojo' },
      { code: 'A', name: 'Azul' },
    ] as faccion[];
    const partidaDemo = service.crearDemo(2, config, facciones, []);
    expect(partidaDemo.map.tiles.length).toBe(300);
    expect(partidaDemo.players.length).toBe(2);
    expect(partidaDemo.units.length).toBe(4);
    expect(partidaDemo.properties.length).toBe(4);
  });
});
