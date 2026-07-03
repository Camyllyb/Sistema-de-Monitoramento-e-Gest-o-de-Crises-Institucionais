export interface AcaoCriseResponseDTO {
  id: number;
  criseId: number;
  tipo: string;
  descricao: string;
  dataAcao: string;
  executorId: number;
  executorNome: string;
}

export interface AcaoCriseCreateDTO {
  tipo: string;
  descricao: string;
  dataAcao?: string;
}
