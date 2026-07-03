import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UsuarioAutenticadoDTO } from '../../../core/models/auth.model';

interface ItemNav {
  label: string;
  rota: string;
  icone: string;
  roles?: string[];
}

const ITENS_NAV: ItemNav[] = [
  { label: 'Dashboard', rota: '/dashboard', icone: 'dashboard' },
  { label: 'Crises', rota: '/crises', icone: 'report_problem' },
  { label: 'Relatórios', rota: '/relatorios', icone: 'description' },
  { label: 'Cenários', rota: '/cenarios', icone: 'auto_awesome', roles: ['ADMIN'] },
  { label: 'Tipos de crise', rota: '/tipos-crise', icone: 'category', roles: ['ADMIN'] },
  { label: 'Departamentos', rota: '/departamentos', icone: 'apartment', roles: ['ADMIN'] },
  { label: 'Campi', rota: '/campi', icone: 'location_city', roles: ['ADMIN'] },
  { label: 'Instituições', rota: '/instituicoes', icone: 'account_balance', roles: ['ADMIN'] },
  { label: 'Usuários', rota: '/usuarios', icone: 'group', roles: ['ADMIN'] },
  { label: 'Auditoria', rota: '/auditoria', icone: 'history', roles: ['ADMIN', 'GERENTE'] },
];

@Component({
  selector: 'app-app-shell',
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.scss',
})
export class AppShellComponent {
  constructor(private authService: AuthService, private router: Router) {}

  get usuario(): UsuarioAutenticadoDTO | null {
    return this.authService.getUsuario();
  }

  get itensNav(): ItemNav[] {
    return ITENS_NAV.filter((item) => !item.roles || this.authService.hasRole(item.roles));
  }

  logout(): void {
    this.authService.logout().subscribe({
      complete: () => this.router.navigate(['/auth/login']),
      error: () => this.router.navigate(['/auth/login']),
    });
  }
}
