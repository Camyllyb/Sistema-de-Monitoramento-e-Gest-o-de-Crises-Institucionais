import { Component, Input, OnChanges } from '@angular/core';
import { STATUS_CRISE_OPTIONS } from '../../../core/constants/status-crise.constants';
import { NIVEL_CRISE_OPTIONS } from '../../../core/constants/nivel-crise.constants';

@Component({
  selector: 'app-status-badge',
  templateUrl: './status-badge.component.html',
  styleUrl: './status-badge.component.scss',
})
export class StatusBadgeComponent implements OnChanges {
  @Input({ required: true }) value!: string;
  @Input({ required: true }) kind!: 'status' | 'nivel';

  label = '';
  color = '#64748B';

  ngOnChanges(): void {
    const opcoes = this.kind === 'status' ? STATUS_CRISE_OPTIONS : NIVEL_CRISE_OPTIONS;
    const encontrada = opcoes.find((opcao) => opcao.value === this.value);
    this.label = encontrada?.label ?? this.value;
    this.color = encontrada?.color ?? '#64748B';
  }
}
