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
    servicio.obtener(11).subscribe((b) => expect(b instanceof Blob).toBe(true));
    const r = http.expectOne(`${API_BASE_URL}/games/11/report.pdf`);
    expect(r.request.method).toBe('GET');
    expect(r.request.responseType).toBe('blob');
    r.flush(new Blob(['%PDF-1.4'], { type: 'application/pdf' }));
    http.verify();
  });
});
