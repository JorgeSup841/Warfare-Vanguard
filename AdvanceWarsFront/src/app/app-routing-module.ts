import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Iniciosesioncomponent } from './components/iniciosesioncomponent/iniciosesioncomponent';
import { Registercomponent } from './components/registercomponent/registercomponent';
import { Juegocomponent } from './components/juegocomponent/juegocomponent';
import { Verificarcomponent } from './components/verificarcomponent/verificarcomponent';

const routes: Routes = [
  { path: '', component: Iniciosesioncomponent },
  { path: 'register', component: Registercomponent },
  {path : 'juego', component: Juegocomponent},
  { path: 'verificar', component: Verificarcomponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
/**
 * Sistema de redireccion de algunos componentes para facilitar el flujo de manejo de la web
 */
// skipcq: JS-0327
export class AppRoutingModule {}
