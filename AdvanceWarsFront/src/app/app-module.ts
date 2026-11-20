
import {
  NgModule,
  provideBrowserGlobalErrorListeners
} from '@angular/core';

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
import { Tablerocomponent } from './components/tablerocomponent/tablerocomponent';
import { Phasercomponent } from './components/phasercomponent/phasercomponent';

import { Resultadoscomponent } from './components/resultadoscomponent/resultadoscomponent';
import { Admincomponent } from './components/admincomponent/admincomponent';

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
    Tablerocomponent,
    Phasercomponent,
    Resultadoscomponent,
    Admincomponent
  ],

  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule
  ],

  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient()
  ],

  bootstrap: [
    App
  ]
})
/** Módulo raíz de la aplicación AURA WARS. */
export class AppModule {
  static readonly identificador = 'AURA WARS';
}
