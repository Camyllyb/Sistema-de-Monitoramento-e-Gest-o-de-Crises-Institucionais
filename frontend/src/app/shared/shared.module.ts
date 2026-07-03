import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { MaterialModule } from './material/material.module';
import { AppShellComponent } from './components/app-shell/app-shell.component';
import { StatusBadgeComponent } from './components/status-badge/status-badge.component';

@NgModule({
  declarations: [AppShellComponent, StatusBadgeComponent],
  imports: [CommonModule, RouterModule, MaterialModule],
  exports: [MaterialModule, RouterModule, AppShellComponent, StatusBadgeComponent],
})
export class SharedModule {}
