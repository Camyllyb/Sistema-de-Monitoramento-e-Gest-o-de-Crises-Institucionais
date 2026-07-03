export interface Option {
  value: string;
  label: string;
}

export const TIPO_CRISE_OPTIONS: Option[] = [
  { value: 'TECNOLOGIA', label: 'Tecnologia' },
  { value: 'LGPD_DADOS', label: 'LGPD / Dados' },
  { value: 'INFRAESTRUTURA', label: 'Infraestrutura' },
  { value: 'ACADEMICA', label: 'Acadêmica' },
  { value: 'COMUNICACAO', label: 'Comunicação' },
  { value: 'SEGURANCA', label: 'Segurança' },
  { value: 'SAUDE', label: 'Saúde' },
  { value: 'ADMINISTRATIVA', label: 'Administrativa' },
];

export const TIPO_ACAO_CRISE_OPTIONS: Option[] = [
  { value: 'CONTENCAO', label: 'Contenção' },
  { value: 'COMUNICACAO', label: 'Comunicação' },
  { value: 'MONITORAMENTO', label: 'Monitoramento' },
  { value: 'RESOLUCAO', label: 'Resolução' },
];
