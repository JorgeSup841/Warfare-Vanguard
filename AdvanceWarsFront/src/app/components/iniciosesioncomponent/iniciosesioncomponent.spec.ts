import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Iniciosesioncomponent } from './iniciosesioncomponent';

describe('Iniciosesioncomponent', () => {
  let component: Iniciosesioncomponent;
  let fixture: ComponentFixture<Iniciosesioncomponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Iniciosesioncomponent],
    }).compileComponents();

    fixture = TestBed.createComponent(Iniciosesioncomponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
