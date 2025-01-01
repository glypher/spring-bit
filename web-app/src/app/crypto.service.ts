import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class CryptoService {
  private apiUrl = 'http://localhost:8080/crypto/';

  constructor(private http: HttpClient) { }

  getCryptoData(symbol: string): Observable<any> {
    return this.http.get<any>(this.apiUrl + symbol + "/quote");
  }
}
