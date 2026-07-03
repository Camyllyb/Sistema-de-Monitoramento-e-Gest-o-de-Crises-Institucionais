import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CampiComponent } from './pages/campi/campi.component';

const routes: Routes = [{ path: '', component: CampiComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CampiRoutingModule {}
