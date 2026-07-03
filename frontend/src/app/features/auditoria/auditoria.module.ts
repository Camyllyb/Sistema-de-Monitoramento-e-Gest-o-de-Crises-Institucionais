import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AuditoriaRoutingModule } from './auditoria-routing.module';
import { AuditoriaComponent } from './pages/auditoria/auditoria.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [AuditoriaComponent],
  imports: [CommonModule, AuditoriaRoutingModule, SharedModule],
})
export class AuditoriaModule {}
