export interface CriseResponseDTO {
  id: number;
  titulo: string;
  descricao: string;
  tipo: string;
  nivel: string;
  status: string;
  responsavelId: number;
  responsavelNome: string;
  criadoPorId: number;
  criadoPorNome: string;
  dataCriacao: string;
  dataAtualizacao: string;
}

export interface CriseCreateDTO {
  titulo: string;
  descricao: string;
  tipo: string;
  nivel: string;
  responsavelId: number;
}

export type CriseUpdateDTO = CriseCreateDTO;

export interface CriseStatusDTO {
  status: string;
}

export interface CriseFiltro {
  status?: string;
  nivel?: string;
  responsavelId?: number;
  criadoPorId?: number;
}
