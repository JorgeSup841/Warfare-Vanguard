
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

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

import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';
import { jugadorGuard } from './guards/jugador.guard';

/**
 * Define las rutas de navegación de AURA WARS.
 *
 * Las pantallas de autenticación son públicas.
 * Las funcionalidades de partidas requieren una sesión de jugador.
 * Las funcionalidades administrativas requieren el rol ADMIN.
 */
const routes: Routes = [
  {
    path: '',
    component: Iniciosesioncomponent
  },
  {
    path: 'register',
    component: Registercomponent
  },
  {
    path: 'verificar',
    component: Verificarcomponent
  },
  {
    path: 'recuperar',
    component: Recuperarcomponent
  },
  {
    path: 'tablero-demo',
    component: Juegocomponent,
    data: { demo: true }
  },
  {
    path: 'panel',
    component: Panelcomponent,
    canActivate: [authGuard]
  },
  {
    path: 'partidas/nueva',
    component: Crearpartidacomponent,
    canActivate: [authGuard, jugadorGuard]
  },
  {
    path: 'partidas/:id/configurar',
    component: Configurarpartidacomponent,
    canActivate: [authGuard, jugadorGuard]
  },
  {
    path: 'partidas/:id/jugar',
    component: Juegocomponent,
    canActivate: [authGuard, jugadorGuard]
  },
  {
    path: 'partidas/:id/resultados',
    component: Resultadoscomponent,
    canActivate: [authGuard, jugadorGuard]
  },
  {
    path: 'partidas',
    component: Partidascomponent,
    canActivate: [authGuard, jugadorGuard]
  },
  {
    path: 'admin',
    component: Admincomponent,
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'juego',
    redirectTo: 'partidas',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: ''
  }
];

/**
 * Módulo de enrutamiento principal de AURA WARS.
 *
 * Configura Angular Router y centraliza las rutas de la aplicación.
 * Aplica los guards para proteger las pantallas según la autenticación
 * y los permisos del usuario.
 *
 * @class AppRoutingModule
 */
@NgModule({
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [
    RouterModule
  ]
})
export class AppRoutingModule {

  /**
   * Configuración de rutas registrada en Angular Router.
   * Permite consultar la estructura de navegación de la aplicación.
   */
  static readonly rutas: Routes = routes;
}
