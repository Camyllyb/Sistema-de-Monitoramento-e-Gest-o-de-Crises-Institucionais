import { environment } from '../../../environments/environment';

const BACKEND_PORT = '8080';

/**
 * O apiUrl fixo em environment.ts só funciona quando o frontend é acessado
 * via localhost. Em GitHub Codespaces (aberto pelo navegador, não pelo VS Code
 * Desktop) cada porta forwarded vira um host público próprio
 * (ex: meu-codespace-4200.app.github.dev), então "localhost:8080" não resolve
 * pro backend. Detecta esse padrão e monta a URL do backend correspondente.
 */
export function resolveApiUrl(): string {
  const { hostname, protocol } = window.location;

  if (hostname === 'localhost' || hostname === '127.0.0.1') {
    return environment.apiUrl;
  }

  const codespacesMatch = hostname.match(/^(.*)-\d+\.app\.github\.dev$/);
  if (codespacesMatch) {
    const nomeCodespace = codespacesMatch[1];
    return `${protocol}//${nomeCodespace}-${BACKEND_PORT}.app.github.dev/api`;
  }

  return environment.apiUrl;
}
