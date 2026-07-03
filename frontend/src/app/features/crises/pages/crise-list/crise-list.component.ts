import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CriseService } from '../../services/crise.service';
import { AuthService } from '../../../../core/services/auth.service';
import { CriseResponseDTO } from '../../models/crise.model';
import { STATUS_CRISE_OPTIONS } from '../../../../core/constants/status-crise.constants';
import { NIVEL_CRISE_OPTIONS } from '../../../../core/constants/nivel-crise.constants';

@Component({
  selector: 'app-crise-list',
  templateUrl: './crise-list.component.html',
  styleUrl: './crise-list.component.scss',
})
export class CriseListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private criseService = inject(CriseService);
  private authService = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  crises: CriseResponseDTO[] = [];
  carregando = false;
  colunas = ['titulo', 'tipo', 'nivel', 'status', 'responsavelNome', 'dataCriacao', 'acoes'];

  statusOptions = STATUS_CRISE_OPTIONS;
  nivelOptions = NIVEL_CRISE_OPTIONS;

  filtroForm = this.fb.group({
    status: [''],
    nivel: [''],
  });

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    this.filtroForm.patchValue({
      status: params.get('status') ?? '',
      nivel: params.get('nivel') ?? '',
    });
    this.carregar();
  }

  get podeCriar(): boolean {
    return this.authService.hasRole(['ADMIN', 'GERENTE']);
  }

  get podeEditar(): boolean {
    return this.authService.hasRole(['ADMIN', 'GERENTE']);
  }

  get podeExcluir(): boolean {
    return this.authService.hasRole(['ADMIN']);
  }

  filtrar(): void {
    const { status, nivel } = this.filtroForm.getRawValue();
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { status: status || null, nivel: nivel || null },
      queryParamsHandling: 'merge',
    });
    this.carregar();
  }

  carregar(): void {
    this.carregando = true;
    const { status, nivel } = this.filtroForm.getRawValue();

    this.criseService
      .list({ status: status || undefined, nivel: nivel || undefined })
      .subscribe({
        next: (crises) => {
          this.crises = crises;
          this.carregando = false;
        },
        error: () => {
          this.carregando = false;
          this.snackBar.open('Não foi possível carregar as crises.', 'Fechar', { duration: 4000 });
        },
      });
  }

  abrirDetalhe(crise: CriseResponseDTO): void {
    this.router.navigate(['/crises', crise.id]);
  }

  editar(crise: CriseResponseDTO, evento: Event): void {
    evento.stopPropagation();
    this.router.navigate(['/crises', crise.id, 'editar']);
  }

  excluir(crise: CriseResponseDTO, evento: Event): void {
    evento.stopPropagation();
    if (!confirm(`Excluir a crise "${crise.titulo}"?`)) {
      return;
    }
    this.criseService.delete(crise.id).subscribe({
      next: () => this.carregar(),
      error: () => this.snackBar.open('Não foi possível excluir a crise.', 'Fechar', { duration: 4000 }),
    });
  }
}
