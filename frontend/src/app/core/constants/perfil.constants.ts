/**
 * O documento de concepção menciona um 4º perfil, VISUALIZADOR, mas o backend
 * atual (PerfilNome.java) só define estes três. RoleGuard e telas devem ser
 * escritos de forma genérica para não exigir refatoração se ele for adicionado.
 */
export const PERFIS = ['ADMIN', 'GERENTE', 'ANALISTA'] as const;

export type Perfil = (typeof PERFIS)[number];
