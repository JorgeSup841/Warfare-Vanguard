
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { Verificarcomponent } from './verificarcomponent';

describe('Verificarcomponent', () => {

  let component: Verificarcomponent;
  let fixture: ComponentFixture<Verificarcomponent>;

  beforeEach(async () => {

    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule,
        RouterModule.forRoot([])
      ],
      declarations: [
        Verificarcomponent
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Verificarcomponent);
    component = fixture.componentInstance;

    await fixture.whenStable();
  });

  it('debe crear el componente correctamente', () => {
    expect(component).toBeTruthy();
  });

});
