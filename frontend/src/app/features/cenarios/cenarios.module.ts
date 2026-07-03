import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CenariosRoutingModule } from './cenarios-routing.module';
import { CenariosComponent } from './pages/cenarios/cenarios.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [CenariosComponent],
  imports: [CommonModule, CenariosRoutingModule, SharedModule],
})
export class CenariosModule {}
