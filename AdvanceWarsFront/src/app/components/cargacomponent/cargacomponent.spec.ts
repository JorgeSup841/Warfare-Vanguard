import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Cargacomponent } from './cargacomponent';

describe('Cargacomponent', () => {
  let component: Cargacomponent;
  let fixture: ComponentFixture<Cargacomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Cargacomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Cargacomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
