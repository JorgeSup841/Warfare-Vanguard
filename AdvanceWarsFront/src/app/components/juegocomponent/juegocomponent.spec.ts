import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Juegocomponent } from './juegocomponent';

describe('Juegocomponent', () => {
  let component: Juegocomponent;
  let fixture: ComponentFixture<Juegocomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Juegocomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Juegocomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
