export interface LoginRequestDTO {
  email: string;
  senha: string;
}

export interface UsuarioAutenticadoDTO {
  id: number;
  nome: string;
  email: string;
  perfil: string;
}

export interface LoginResponseDTO {
  token: string;
  tipo: string;
  expiraEm: number;
  usuario: UsuarioAutenticadoDTO;
}
