import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ReporteService } from './reporte.service';
import { API_BASE_URL } from './api-config';
describe('ReporteService', () => {
  it('descarga un PDF auténtico vía el backend directo', () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const servicio = TestBed.inject(ReporteService);
    const http = TestBed.inject(HttpTestingController);
    servicio.obtener(11).subscribe((archivo) => expect(archivo instanceof Blob).toBe(true));
    const peticion = http.expectOne(`${API_BASE_URL}/games/11/report.pdf`);
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.responseType).toBe('blob');
    peticion.flush(new Blob(['%PDF-1.4'], { type: 'application/pdf' }));
    http.verify();
  });
});
