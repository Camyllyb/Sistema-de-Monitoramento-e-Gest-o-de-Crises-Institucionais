import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TiposCriseRoutingModule } from './tipos-crise-routing.module';
import { TiposCriseComponent } from './pages/tipos-crise/tipos-crise.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [TiposCriseComponent],
  imports: [CommonModule, TiposCriseRoutingModule, SharedModule],
})
export class TiposCriseModule {}
