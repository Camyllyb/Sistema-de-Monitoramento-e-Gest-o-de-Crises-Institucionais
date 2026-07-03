import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TiposCriseComponent } from './pages/tipos-crise/tipos-crise.component';

const routes: Routes = [{ path: '', component: TiposCriseComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TiposCriseRoutingModule {}
