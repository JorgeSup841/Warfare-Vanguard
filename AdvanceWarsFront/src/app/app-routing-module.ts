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
import { authGuard } from './guards/auth-guard';
import { adminGuard } from './guards/admin-guard';
import { jugadorGuard } from './guards/jugador-guard';
import { invitadoGuard } from './guards/invitado-guard';

const routes: Routes = [
  { path: '', component: Iniciosesioncomponent, canActivate: [invitadoGuard] },
  { path: 'register', component: Registercomponent },
  { path: 'verificar', component: Verificarcomponent },
  { path: 'recuperar', component: Recuperarcomponent },
  { path: 'tablero-demo', component: Juegocomponent, data: { demo: true } },
  { path: 'panel', component: Panelcomponent, canActivate: [authGuard] },
  { path: 'partidas', component: Partidascomponent, canActivate: [authGuard, jugadorGuard] },
  {
    path: 'partidas/nueva',
    component: Crearpartidacomponent,
    canActivate: [authGuard, jugadorGuard],
  },
  {
    path: 'partidas/:id/configurar',
    component: Configurarpartidacomponent,
    canActivate: [authGuard, jugadorGuard],
  },
  { path: 'partidas/:id/jugar', component: Juegocomponent, canActivate: [authGuard, jugadorGuard] },
  {
    path: 'partidas/:id/resultados',
    component: Resultadoscomponent,
    canActivate: [authGuard, jugadorGuard],
  },
  { path: 'admin', component: Admincomponent, canActivate: [authGuard, adminGuard] },
  { path: 'juego', redirectTo: 'partidas', pathMatch: 'full' },
  { path: '**', redirectTo: '' },
];
/** Configura la navegación y las restricciones de acceso. */
@NgModule({ imports: [RouterModule.forRoot(routes)], exports: [RouterModule] })
export class AppRoutingModule {
  readonly rutas: Routes = routes;
}
