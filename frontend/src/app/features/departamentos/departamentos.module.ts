import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DepartamentosRoutingModule } from './departamentos-routing.module';
import { DepartamentosComponent } from './pages/departamentos/departamentos.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [DepartamentosComponent],
  imports: [CommonModule, DepartamentosRoutingModule, SharedModule],
})
export class DepartamentosModule {}
