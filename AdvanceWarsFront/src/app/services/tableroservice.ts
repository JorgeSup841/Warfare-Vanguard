import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { nuevapartida, partidaresumen, tablero } from '../models/tablero';

@Injectable({
  providedIn: 'root'
})
export class tableroservice {
  private http = inject(HttpClient);
  private url = 'http://localhost:8080/api/games';


  crearPartida(datos: nuevapartida): Observable<partidaresumen> {
    return this.http.post<partidaresumen>(this.url, datos, { headers: this.headers() });
  }


  getTablero(id: number): Observable<tablero> {
    return this.http.get<tablero>(this.url + '/' + id + '/map', { headers: this.headers() });
  }


  private headers(): HttpHeaders {
    return new HttpHeaders({ Authorization: 'Bearer ' + localStorage.getItem('jwtToken') });
  }
}
