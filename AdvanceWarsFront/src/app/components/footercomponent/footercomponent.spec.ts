import { TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Footercomponent } from './footercomponent';
describe('Footercomponent', () => {
  it('declara el componente', async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule, FormsModule, RouterModule.forRoot([])],
      declarations: [Footercomponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    expect(TestBed.createComponent(Footercomponent).componentInstance).toBeTruthy();
  });
});
