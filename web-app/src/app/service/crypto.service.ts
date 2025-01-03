import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';
import {BehaviorSubject, map, Observable} from 'rxjs';
import {CryptoQuote, CryptoType} from "./crypto.types";
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class CryptoService {
  private apiUrl = environment.apiUrl;

  private selectedSymbolSource = new BehaviorSubject<CryptoType>(CryptoType.DEFAULT_TYPE);

  selectedSymbol = this.selectedSymbolSource.asObservable();

  constructor(private http: HttpClient) {
  }

  onSymbolChange(newCryptoType: CryptoType) {
    this.selectedSymbolSource.next(newCryptoType);
  }

  loadCryptos() {
    return this.http.get<CryptoType>(this.apiUrl + "cryptos").pipe(
      map(reply => Object.assign(new Array<CryptoType>(), reply))
    );
  }

  getCryptoData(crypto: CryptoType): Observable<CryptoQuote[]> {
    return this.http.get<CryptoQuote>(this.apiUrl + crypto.symbol + "/quote").pipe(
      map(reply => Object.assign(new Array<CryptoQuote>(), reply))
    );
  }
}
