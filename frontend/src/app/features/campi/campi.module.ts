import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CampiRoutingModule } from './campi-routing.module';
import { CampiComponent } from './pages/campi/campi.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [CampiComponent],
  imports: [CommonModule, CampiRoutingModule, SharedModule],
})
export class CampiModule {}
