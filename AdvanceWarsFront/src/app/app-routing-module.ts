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
export class AppRoutingModule {}
