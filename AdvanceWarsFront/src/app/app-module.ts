import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { provideHttpClient } from '@angular/common/http';
import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
import { Footercomponent } from './components/footercomponent/footercomponent';
import { Iniciosesioncomponent } from './components/iniciosesioncomponent/iniciosesioncomponent';
import { Registercomponent } from './components/registercomponent/registercomponent';
import { Verificarcomponent } from './components/verificarcomponent/verificarcomponent';
import { Recuperarcomponent } from './components/recuperarcomponent/recuperarcomponent';
import { Panelcomponent } from './components/panelcomponent/panelcomponent';
import { Partidascomponent } from './components/partidascomponent/partidascomponent';
import { Crearpartidacomponent } from './components/crearpartidacomponent/crearpartidacomponent';
import { Configurarpartidacomponent } from './components/configurarpartidacomponent/configurarpartidacomponent';
import { Juegocomponent } from './components/juegocomponent/juegocomponent';
import { Resultadoscomponent } from './components/resultadoscomponent/resultadoscomponent';
import { Admincomponent } from './components/admincomponent/admincomponent';
import { Juegounidadescomponent } from './components/juegounidadescomponent/juegounidadescomponent';
import { Juegoaccionescomponent } from './components/juegoaccionescomponent/juegoaccionescomponent';
import { Juegohistorialcomponent } from './components/juegohistorialcomponent/juegohistorialcomponent';
import { Cargacomponent } from './components/cargacomponent/cargacomponent';
/** Módulo raíz de la aplicación AURA WARS. */
@NgModule({
  declarations: [
    App,
    Navbarcomponent,
    Footercomponent,
    Iniciosesioncomponent,
    Registercomponent,
    Verificarcomponent,
    Recuperarcomponent,
    Panelcomponent,
    Partidascomponent,
    Crearpartidacomponent,
    Configurarpartidacomponent,
    Juegocomponent,
    Resultadoscomponent,
    Admincomponent,
    Juegounidadescomponent,
    Juegoaccionescomponent,
    Juegohistorialcomponent,
    Cargacomponent,
  ],
  imports: [BrowserModule, FormsModule, AppRoutingModule],
  providers: [provideBrowserGlobalErrorListeners(), provideHttpClient()],
  bootstrap: [App],
})
export class AppModule {
  readonly identificador = 'AURA WARS';
}
