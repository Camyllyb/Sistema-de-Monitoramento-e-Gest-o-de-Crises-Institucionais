import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { resolveApiUrl } from '../../../core/utils/api-url.util';
import { ApiResponseDTO } from '../../../core/models/api-response.model';
import {
  CriseCreateDTO,
  CriseFiltro,
  CriseResponseDTO,
  CriseStatusDTO,
  CriseUpdateDTO,
} from '../models/crise.model';
import { AcaoCriseCreateDTO, AcaoCriseResponseDTO } from '../models/acao.model';

@Injectable({
  providedIn: 'root',
})
export class CriseService {
  private readonly apiUrl = `${resolveApiUrl()}/crises`;

  constructor(private http: HttpClient) {}

  list(filtro: CriseFiltro = {}): Observable<CriseResponseDTO[]> {
    let params = new HttpParams();
    if (filtro.status) params = params.set('status', filtro.status);
    if (filtro.nivel) params = params.set('nivel', filtro.nivel);
    if (filtro.responsavelId) params = params.set('responsavelId', filtro.responsavelId);
    if (filtro.criadoPorId) params = params.set('criadoPorId', filtro.criadoPorId);

    return this.http
      .get<ApiResponseDTO<CriseResponseDTO[]>>(this.apiUrl, { params })
      .pipe(map((resposta) => resposta.dados));
  }

  getById(id: number): Observable<CriseResponseDTO> {
    return this.http
      .get<ApiResponseDTO<CriseResponseDTO>>(`${this.apiUrl}/${id}`)
      .pipe(map((resposta) => resposta.dados));
  }

  create(dto: CriseCreateDTO): Observable<CriseResponseDTO> {
    return this.http
      .post<ApiResponseDTO<CriseResponseDTO>>(this.apiUrl, dto)
      .pipe(map((resposta) => resposta.dados));
  }

  update(id: number, dto: CriseUpdateDTO): Observable<CriseResponseDTO> {
    return this.http
      .put<ApiResponseDTO<CriseResponseDTO>>(`${this.apiUrl}/${id}`, dto)
      .pipe(map((resposta) => resposta.dados));
  }

  updateStatus(id: number, dto: CriseStatusDTO): Observable<CriseResponseDTO> {
    return this.http
      .patch<ApiResponseDTO<CriseResponseDTO>>(`${this.apiUrl}/${id}/status`, dto)
      .pipe(map((resposta) => resposta.dados));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  listAcoes(criseId: number): Observable<AcaoCriseResponseDTO[]> {
    return this.http
      .get<ApiResponseDTO<AcaoCriseResponseDTO[]>>(`${this.apiUrl}/${criseId}/acoes`)
      .pipe(map((resposta) => resposta.dados));
  }

  createAcao(criseId: number, dto: AcaoCriseCreateDTO): Observable<AcaoCriseResponseDTO> {
    return this.http
      .post<ApiResponseDTO<AcaoCriseResponseDTO>>(`${this.apiUrl}/${criseId}/acoes`, dto)
      .pipe(map((resposta) => resposta.dados));
  }
}
