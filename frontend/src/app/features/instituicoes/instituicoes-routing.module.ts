import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InstituicoesComponent } from './pages/instituicoes/instituicoes.component';

const routes: Routes = [{ path: '', component: InstituicoesComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class InstituicoesRoutingModule {}
