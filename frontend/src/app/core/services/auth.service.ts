import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponseDTO } from '../models/api-response.model';
import { LoginRequestDTO, LoginResponseDTO, UsuarioAutenticadoDTO } from '../models/auth.model';

const TOKEN_KEY = 'gestao_crises_token';
const USUARIO_KEY = 'gestao_crises_usuario';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  login(credenciais: LoginRequestDTO): Observable<ApiResponseDTO<LoginResponseDTO>> {
    return this.http.post<ApiResponseDTO<LoginResponseDTO>>(`${this.apiUrl}/login`, credenciais).pipe(
      tap((resposta) => {
        sessionStorage.setItem(TOKEN_KEY, resposta.dados.token);
        sessionStorage.setItem(USUARIO_KEY, JSON.stringify(resposta.dados.usuario));
      })
    );
  }

  logout(): Observable<ApiResponseDTO<Record<string, string>>> {
    return this.http.post<ApiResponseDTO<Record<string, string>>>(`${this.apiUrl}/logout`, {}).pipe(
      tap(() => this.limparSessao())
    );
  }

  me(): Observable<ApiResponseDTO<UsuarioAutenticadoDTO>> {
    return this.http.get<ApiResponseDTO<UsuarioAutenticadoDTO>>(`${this.apiUrl}/me`);
  }

  limparSessao(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USUARIO_KEY);
  }

  getToken(): string | null {
    return sessionStorage.getItem(TOKEN_KEY);
  }

  getUsuario(): UsuarioAutenticadoDTO | null {
    const raw = sessionStorage.getItem(USUARIO_KEY);
    return raw ? (JSON.parse(raw) as UsuarioAutenticadoDTO) : null;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  hasRole(rolesPermitidos: string[]): boolean {
    const usuario = this.getUsuario();
    return !!usuario && rolesPermitidos.includes(usuario.perfil);
  }
}
