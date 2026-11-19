import { TestBed } from '@angular/core/testing';
import { Generadormapaservice } from './generadormapaservice';

describe('Generadormapaservice', () => {
  let service: Generadormapaservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Generadormapaservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
