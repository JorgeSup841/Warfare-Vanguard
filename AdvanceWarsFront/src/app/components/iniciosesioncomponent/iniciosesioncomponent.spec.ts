import { TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Iniciosesioncomponent } from './iniciosesioncomponent';
describe('Iniciosesioncomponent', () => {
  it('declara el componente', async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule, RouterModule.forRoot([])],
      declarations: [Iniciosesioncomponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    expect(TestBed.createComponent(Iniciosesioncomponent).componentInstance).toBeTruthy();
  });
});
