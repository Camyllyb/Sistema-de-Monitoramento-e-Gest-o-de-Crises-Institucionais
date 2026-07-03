import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppShellComponent } from './shared/components/app-shell/app-shell.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.module').then((m) => m.AuthModule),
  },
  {
    path: 'access-denied',
    loadChildren: () =>
      import('./features/access-denied/access-denied.module').then((m) => m.AccessDeniedModule),
  },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadChildren: () => import('./features/dashboard/dashboard.module').then((m) => m.DashboardModule),
      },
      {
        path: 'crises',
        loadChildren: () => import('./features/crises/crises.module').then((m) => m.CrisesModule),
      },
      {
        path: 'relatorios',
        loadChildren: () => import('./features/relatorios/relatorios.module').then((m) => m.RelatoriosModule),
      },
      {
        path: 'usuarios',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/usuarios/usuarios.module').then((m) => m.UsuariosModule),
      },
      {
        path: 'instituicoes',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () =>
          import('./features/instituicoes/instituicoes.module').then((m) => m.InstituicoesModule),
      },
      {
        path: 'campi',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/campi/campi.module').then((m) => m.CampiModule),
      },
      {
        path: 'departamentos',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () =>
          import('./features/departamentos/departamentos.module').then((m) => m.DepartamentosModule),
      },
      {
        path: 'tipos-crise',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () =>
          import('./features/tipos-crise/tipos-crise.module').then((m) => m.TiposCriseModule),
      },
      {
        path: 'cenarios',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/cenarios/cenarios.module').then((m) => m.CenariosModule),
      },
      {
        path: 'auditoria',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'GERENTE'] },
        loadChildren: () => import('./features/auditoria/auditoria.module').then((m) => m.AuditoriaModule),
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
