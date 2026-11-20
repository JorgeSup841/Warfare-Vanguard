import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { juegoservice } from './juegoservice';
describe('juegoservice', () => {
  it('lee Terrenos.json con la capitalización del archivo', () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const service = TestBed.inject(juegoservice);
    const http = TestBed.inject(HttpTestingController);
    service.getTerrenos().subscribe((terrenos) => expect(terrenos).toEqual([]));
    const req = http.expectOne('assets/data/Terrenos.json');
    expect(req.request.method).toBe('GET');
    req.flush([]);
    http.verify();
  });
});
