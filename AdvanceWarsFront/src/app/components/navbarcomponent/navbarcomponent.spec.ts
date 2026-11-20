import { TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Navbarcomponent } from './navbarcomponent';
describe('Navbarcomponent', () => {
  it('declara el componente', async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule, RouterModule.forRoot([])],
      declarations: [Navbarcomponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    expect(TestBed.createComponent(Navbarcomponent).componentInstance).toBeTruthy();
  });
});
