
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { Juegocomponent } from './juegocomponent';

describe('Juegocomponent', () => {

  let component: Juegocomponent;
  let fixture: ComponentFixture<Juegocomponent>;

  beforeEach(async () => {

    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule,
        RouterModule.forRoot([])
      ],
      declarations: [
        Juegocomponent
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Juegocomponent);
    component = fixture.componentInstance;

    await fixture.whenStable();
  });

  it('debe crear el componente correctamente', () => {
    expect(component).toBeTruthy();
  });

});
