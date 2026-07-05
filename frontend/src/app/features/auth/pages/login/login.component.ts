import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../../core/services/auth.service';
import { ErrorResponseDTO } from '../../../../core/models/api-response.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  carregando = false;

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]],
  });

  entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.carregando = true;
    const { email, senha } = this.form.getRawValue();

    this.authService.login({ email: email!, senha: senha! }).subscribe({
      next: () => {
        this.carregando = false;
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/dashboard';
        this.router.navigateByUrl(returnUrl);
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando = false;
        this.snackBar.open(this.mensagemDeErro(erro), 'Fechar', { duration: 5000 });
      },
    });
  }

  private mensagemDeErro(erro: HttpErrorResponse): string {
    if (erro.status === 0) {
      return 'Não foi possível conectar ao servidor. Verifique se o backend está rodando.';
    }

    const corpo = erro.error as ErrorResponseDTO | undefined;
    if (corpo?.mensagem) {
      return corpo.mensagem;
    }

    if (erro.status === 401) {
      return 'E-mail ou senha inválidos.';
    }

    return `Erro inesperado ao entrar (status ${erro.status}).`;
  }
}
