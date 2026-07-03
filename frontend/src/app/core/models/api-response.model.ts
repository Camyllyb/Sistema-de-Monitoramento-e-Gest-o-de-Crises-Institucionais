export interface ApiResponseDTO<T> {
  status: number;
  mensagem: string;
  dados: T;
}

export interface ErrorResponseDTO {
  status: number;
  erro: string;
  mensagem: string;
  detalhes: string[];
  timestamp: string;
}
