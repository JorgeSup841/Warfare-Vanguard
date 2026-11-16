import { TestBed } from '@angular/core/testing';
import { Juegoservice } from './juegoservice';

describe('Juegoservice', () => {
  let service: Juegoservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Juegoservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
