import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Registercomponent } from './components/registercomponent/registercomponent';
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
import { Footercomponent } from './components/footercomponent/footercomponent';
import { Iniciosesioncomponent } from './components/iniciosesioncomponent/iniciosesioncomponent';

@NgModule({
  declarations: [App, Registercomponent, Navbarcomponent, Footercomponent, Iniciosesioncomponent],
  imports: [BrowserModule, AppRoutingModule],
  providers: [provideBrowserGlobalErrorListeners()],
  bootstrap: [App],
})
/**
 * Módulo principal de la aplicación.
 */
// deepsource-ignore JS-0327
export class AppModule {}
