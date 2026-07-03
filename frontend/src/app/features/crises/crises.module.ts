import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { CrisesRoutingModule } from './crises-routing.module';
import { CriseListComponent } from './pages/crise-list/crise-list.component';
import { CriseFormComponent } from './pages/crise-form/crise-form.component';
import { CriseDetailComponent } from './pages/crise-detail/crise-detail.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [CriseListComponent, CriseFormComponent, CriseDetailComponent],
  imports: [CommonModule, ReactiveFormsModule, CrisesRoutingModule, SharedModule],
})
export class CrisesModule {}
