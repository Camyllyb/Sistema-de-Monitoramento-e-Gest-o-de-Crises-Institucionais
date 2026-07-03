import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { InstituicoesRoutingModule } from './instituicoes-routing.module';
import { InstituicoesComponent } from './pages/instituicoes/instituicoes.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [InstituicoesComponent],
  imports: [CommonModule, InstituicoesRoutingModule, SharedModule],
})
export class InstituicoesModule {}
