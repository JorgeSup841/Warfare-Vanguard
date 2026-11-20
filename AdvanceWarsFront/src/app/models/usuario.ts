export interface usuario {
  id?: number;
  username: string;
  email?: string;
  password?: string;
  role: 'ADMIN' | 'USER';
  coins?: number;
  countryCode?: string;
}

export interface AuthRequest {
  username: string;
  password?: string;
}

export interface AuthResponse {
  token: string;
  role: string;
  username: string;
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

export interface registro {
  username: string;
  email: string;
  password: string;
}


export interface respuestapendiente {
  status: string;
  challengeId: string;
  expiresAt: string;
}


export interface verificacion {
  challengeId: string;
  code: string;
}
