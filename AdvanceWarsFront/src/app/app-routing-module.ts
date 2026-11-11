import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Iniciosesioncomponent } from './components/iniciosesioncomponent/iniciosesioncomponent';
import { Registercomponent } from './components/registercomponent/registercomponent';

const routes: Routes = [
  { path: '', component: Iniciosesioncomponent },
  { path: 'register', component: Registercomponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
/**
 * Sistema de redireccion de algunos componentes para facilitar el flujo de manejo de la web
 */
export class AppRoutingModule {}
