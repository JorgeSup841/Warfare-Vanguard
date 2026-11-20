export type rolusuario = 'USER' | 'ADMIN';
export interface usuario {
  id?: number;
  username: string;
  email?: string;
  role: rolusuario;
  emailVerified?: boolean;
  enabled?: boolean;
  coins?: number;
  countryCode?: string;
}
export interface registro {
  username: string;
  email: string;
  password: string;
}
export interface respuestapendiente {
  status?: string;
  challengeId: string;
  expiresAt?: string;
}
export interface verificacion {
  challengeId: string;
  code: string;
}
export interface usuarioautenticado {
  id: number;
  username: string;
  email: string;
  role: rolusuario | `ROLE_${rolusuario}`;
  emailVerified?: boolean;
  enabled?: boolean;
}
// AuthResponseDTO.java no contiene expiresAt; se obtiene de claim exp del JWT.
export interface respuestalogin {
  accessToken: string;
  tokenType?: string;
  user: usuarioautenticado;
}
export interface desafioautenticacion {
  tipo: 'correo' | 'login';
  challengeId: string;
  correo?: string;
  expiresAt?: string;
}
export interface respuestaMensaje {
  message: string;
}
export interface historial {
  gameId: number;
  id?: number;
  date: string;
  rounds: number;
  won: boolean;
  kills: number;
  losses: number;
  opponents: string[];
}
