import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing-module';
import { HttpClientModule } from '@angular/common/http';
import { App } from './app';
import { Registercomponent } from './components/registercomponent/registercomponent';
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
import { Footercomponent } from './components/footercomponent/footercomponent';
import { Iniciosesioncomponent } from './components/iniciosesioncomponent/iniciosesioncomponent';
import { Juegocomponent } from './components/juegocomponent/juegocomponent';
import { FormsModule} from '@angular/forms';

@NgModule({
  declarations: [App, Registercomponent, Navbarcomponent, Footercomponent, Iniciosesioncomponent, Juegocomponent],
  imports: [BrowserModule, AppRoutingModule,HttpClientModule, FormsModule],
  providers: [provideBrowserGlobalErrorListeners()],
  bootstrap: [App],
})
/**
 * Módulo principal de la aplicación.
 */
// skipcq: JS-0327
export class AppModule {}
