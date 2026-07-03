import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CriseService } from '../../services/crise.service';
import { TIPO_CRISE_OPTIONS } from '../../../../core/constants/tipo-crise.constants';
import { NIVEL_CRISE_OPTIONS } from '../../../../core/constants/nivel-crise.constants';
import { ErrorResponseDTO } from '../../../../core/models/api-response.model';

@Component({
  selector: 'app-crise-form',
  templateUrl: './crise-form.component.html',
  styleUrl: './crise-form.component.scss',
})
export class CriseFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private criseService = inject(CriseService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  tipoOptions = TIPO_CRISE_OPTIONS;
  nivelOptions = NIVEL_CRISE_OPTIONS;

  criseId: number | null = null;
  carregando = false;
  salvando = false;

  form = this.fb.group({
    titulo: ['', [Validators.required]],
    descricao: ['', [Validators.required]],
    tipo: ['', [Validators.required]],
    nivel: ['', [Validators.required]],
    responsavelId: [null as number | null, [Validators.required]],
  });

  get modoEdicao(): boolean {
    return this.criseId !== null;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.criseId = Number(idParam);
      this.carregarCrise(this.criseId);
    }
  }

  private carregarCrise(id: number): void {
    this.carregando = true;
    this.criseService.getById(id).subscribe({
      next: (crise) => {
        this.form.patchValue({
          titulo: crise.titulo,
          descricao: crise.descricao,
          tipo: crise.tipo,
          nivel: crise.nivel,
          responsavelId: crise.responsavelId,
        });
        this.carregando = false;
      },
      error: () => {
        this.carregando = false;
        this.snackBar.open('Não foi possível carregar a crise.', 'Fechar', { duration: 4000 });
        this.router.navigate(['/crises']);
      },
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { titulo, descricao, tipo, nivel, responsavelId } = this.form.getRawValue();
    const dto = {
      titulo: titulo!,
      descricao: descricao!,
      tipo: tipo!,
      nivel: nivel!,
      responsavelId: responsavelId!,
    };

    this.salvando = true;
    const operacao = this.modoEdicao
      ? this.criseService.update(this.criseId!, dto)
      : this.criseService.create(dto);

    operacao.subscribe({
      next: (crise) => {
        this.salvando = false;
        this.router.navigate(['/crises', crise.id]);
      },
      error: (erro) => {
        this.salvando = false;
        const corpo = erro?.error as ErrorResponseDTO | undefined;
        this.snackBar.open(corpo?.mensagem ?? 'Não foi possível salvar a crise.', 'Fechar', { duration: 4000 });
      },
    });
  }

  cancelar(): void {
    this.router.navigate(this.modoEdicao ? ['/crises', this.criseId] : ['/crises']);
  }
}
