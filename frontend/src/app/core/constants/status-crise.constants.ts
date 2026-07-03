export interface OptionColor {
  value: string;
  label: string;
  color: string;
}

export const STATUS_CRISE_OPTIONS: OptionColor[] = [
  { value: 'ABERTA', label: 'Aberta', color: '#3B82F6' },
  { value: 'EM_ANDAMENTO', label: 'Em andamento', color: '#F59E0B' },
  { value: 'RESOLVIDA', label: 'Resolvida', color: '#16A34A' },
  { value: 'ENCERRADA', label: 'Encerrada', color: '#64748B' },
];
