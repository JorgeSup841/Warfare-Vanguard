
import {
  NgModule,
  provideBrowserGlobalErrorListeners
} from '@angular/core';

import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { provideHttpClient } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';

// Componentes generales
import { Navbarcomponent } from './components/navbarcomponent/navbarcomponent';
import { Footercomponent } from './components/footercomponent/footercomponent';

// Autenticación y usuarios
import { Iniciosesioncomponent } from './components/iniciosesioncomponent/iniciosesioncomponent';
import { Registercomponent } from './components/registercomponent/registercomponent';
import { Verificarcomponent } from './components/verificarcomponent/verificarcomponent';
import { Recuperarcomponent } from './components/recuperarcomponent/recuperarcomponent';

// Panel principal
import { Panelcomponent } from './components/panelcomponent/panelcomponent';

// Gestión de partidas
import { Partidascomponent } from './components/partidascomponent/partidascomponent';
import { Crearpartidacomponent } from './components/crearpartidacomponent/crearpartidacomponent';
import { Configurarpartidacomponent } from './components/configurarpartidacomponent/configurarpartidacomponent';

// Juego y Phaser
import { Juegocomponent } from './components/juegocomponent/juegocomponent';
import { Tablerocomponent } from './components/tablerocomponent/tablerocomponent';
import { Phasercomponent } from './components/phasercomponent/phasercomponent';

// Resultados y administración
import { Resultadoscomponent } from './components/resultadoscomponent/resultadoscomponent';
import { Admincomponent } from './components/admincomponent/admincomponent';

@NgModule({

  declarations: [
    App,

    // Componentes generales
    Navbarcomponent,
    Footercomponent,

    // Autenticación
    Iniciosesioncomponent,
    Registercomponent,
    Verificarcomponent,
    Recuperarcomponent,

    // Panel principal
    Panelcomponent,

    // Partidas
    Partidascomponent,
    Crearpartidacomponent,
    Configurarpartidacomponent,

    // Juego
    Juegocomponent,
    Tablerocomponent,
    Phasercomponent,

    // Resultados y administrador
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
export class AppModule {}
