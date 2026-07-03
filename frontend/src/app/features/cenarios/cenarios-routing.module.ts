import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CenariosComponent } from './pages/cenarios/cenarios.component';

const routes: Routes = [{ path: '', component: CenariosComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CenariosRoutingModule {}
