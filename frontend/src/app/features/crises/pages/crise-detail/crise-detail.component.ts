import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CriseService } from '../../services/crise.service';
import { AuthService } from '../../../../core/services/auth.service';
import { CriseResponseDTO } from '../../models/crise.model';
import { AcaoCriseResponseDTO } from '../../models/acao.model';
import { STATUS_CRISE_OPTIONS } from '../../../../core/constants/status-crise.constants';
import { TIPO_ACAO_CRISE_OPTIONS } from '../../../../core/constants/tipo-crise.constants';
import { ErrorResponseDTO } from '../../../../core/models/api-response.model';

const STATUS_QUE_BLOQUEIAM_MUDANCA = ['ENCERRADA'];
const STATUS_QUE_PERMITEM_ACAO_ANALISTA = ['ABERTA', 'EM_ANDAMENTO'];

@Component({
  selector: 'app-crise-detail',
  templateUrl: './crise-detail.component.html',
  styleUrl: './crise-detail.component.scss',
})
export class CriseDetailComponent implements OnInit {
  private fb = inject(FormBuilder);
  private criseService = inject(CriseService);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  crise: CriseResponseDTO | null = null;
  acoes: AcaoCriseResponseDTO[] = [];
  carregando = false;
  registrandoAcao = false;
  alterandoStatus = false;

  statusOptions = STATUS_CRISE_OPTIONS;
  tipoAcaoOptions = TIPO_ACAO_CRISE_OPTIONS;

  acaoForm = this.fb.group({
    tipo: ['', [Validators.required]],
    descricao: ['', [Validators.required]],
  });

  statusForm = this.fb.group({
    status: ['', [Validators.required]],
  });

  private criseId!: number;

  ngOnInit(): void {
    this.criseId = Number(this.route.snapshot.paramMap.get('id'));
    this.carregar();
  }

  get podeGerenciar(): boolean {
    return this.authService.hasRole(['ADMIN', 'GERENTE']);
  }

  get podeRegistrarAcao(): boolean {
    if (!this.crise) return false;
    if (this.authService.hasRole(['ADMIN', 'GERENTE'])) return true;
    return (
      this.authService.hasRole(['ANALISTA']) &&
      STATUS_QUE_PERMITEM_ACAO_ANALISTA.includes(this.crise.status)
    );
  }

  get statusBloqueado(): boolean {
    return !!this.crise && STATUS_QUE_BLOQUEIAM_MUDANCA.includes(this.crise.status);
  }

  get opcoesStatusDisponiveis() {
    return this.statusOptions.filter((opcao) => opcao.value !== this.crise?.status);
  }

  carregar(): void {
    this.carregando = true;
    this.criseService.getById(this.criseId).subscribe({
      next: (crise) => {
        this.crise = crise;
        this.statusForm.patchValue({ status: crise.status });
        this.carregando = false;
      },
      error: () => {
        this.carregando = false;
        this.snackBar.open('Não foi possível carregar a crise.', 'Fechar', { duration: 4000 });
        this.router.navigate(['/crises']);
      },
    });
    this.carregarAcoes();
  }

  carregarAcoes(): void {
    this.criseService.listAcoes(this.criseId).subscribe({
      next: (acoes) => (this.acoes = acoes),
      error: () => this.snackBar.open('Não foi possível carregar as ações.', 'Fechar', { duration: 4000 }),
    });
  }

  registrarAcao(): void {
    if (this.acaoForm.invalid) {
      this.acaoForm.markAllAsTouched();
      return;
    }

    const { tipo, descricao } = this.acaoForm.getRawValue();
    this.registrandoAcao = true;
    this.criseService.createAcao(this.criseId, { tipo: tipo!, descricao: descricao! }).subscribe({
      next: () => {
        this.registrandoAcao = false;
        this.acaoForm.reset();
        this.carregarAcoes();
      },
      error: (erro) => {
        this.registrandoAcao = false;
        const corpo = erro?.error as ErrorResponseDTO | undefined;
        this.snackBar.open(corpo?.mensagem ?? 'Não foi possível registrar a ação.', 'Fechar', { duration: 4000 });
      },
    });
  }

  alterarStatus(): void {
    if (this.statusForm.invalid) return;
    const { status } = this.statusForm.getRawValue();

    this.alterandoStatus = true;
    this.criseService.updateStatus(this.criseId, { status: status! }).subscribe({
      next: (crise) => {
        this.crise = crise;
        this.alterandoStatus = false;
        this.snackBar.open('Status atualizado.', 'Fechar', { duration: 3000 });
      },
      error: (erro) => {
        this.alterandoStatus = false;
        const corpo = erro?.error as ErrorResponseDTO | undefined;
        this.snackBar.open(corpo?.mensagem ?? 'Não foi possível alterar o status.', 'Fechar', { duration: 4000 });
      },
    });
  }

  editar(): void {
    this.router.navigate(['/crises', this.criseId, 'editar']);
  }
}
