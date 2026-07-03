import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CriseListComponent } from './pages/crise-list/crise-list.component';
import { CriseFormComponent } from './pages/crise-form/crise-form.component';
import { CriseDetailComponent } from './pages/crise-detail/crise-detail.component';
import { roleGuard } from '../../core/guards/role.guard';

const routes: Routes = [
  {
    path: '',
    component: CriseListComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN', 'GERENTE', 'ANALISTA'] },
  },
  {
    path: 'nova',
    component: CriseFormComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN', 'GERENTE'] },
  },
  {
    path: ':id',
    component: CriseDetailComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN', 'GERENTE', 'ANALISTA'] },
  },
  {
    path: ':id/editar',
    component: CriseFormComponent,
    canActivate: [roleGuard],
    data: { roles: ['ADMIN', 'GERENTE'] },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CrisesRoutingModule {}
