import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';
import {BehaviorSubject, map, Observable} from 'rxjs';
import {CryptoQuote, CryptoType} from "./service.types";
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class CryptoService {
  private apiUrl = environment.serviceHost? `${window.location.protocol}//${environment.serviceHost}${environment.apiPath}` : environment.apiPath;

  private selectedSymbolSource = new BehaviorSubject<CryptoType>(CryptoType.DEFAULT_TYPE);
  selectedSymbol = this.selectedSymbolSource.asObservable();


  private isServiceAvailableSource = new BehaviorSubject<boolean>(false);
  isServiceAvailable = this.isServiceAvailableSource.asObservable();

  constructor(private http: HttpClient) {}

  checkServiceStatus(): void {
    this.isAlive().subscribe({
      next: () => {
        this.isServiceAvailableSource.next(true)
      },
      error: () => this.isServiceAvailableSource.next(false)
    });
  }

  onSymbolChange(newCryptoType: CryptoType) {
    this.selectedSymbolSource.next(newCryptoType);
  }

  // Public APIs
  isAlive() {
    return this.http.get(this.apiUrl + "live", { responseType: 'text' });
  }

  loadCryptos() {
    return this.http.get<CryptoType>(this.apiUrl + "cryptos").pipe(
      map(reply => Object.assign(new Array<CryptoType>(), reply))
    );
  }

  getCryptoData(crypto: CryptoType): Observable<CryptoQuote[]> {
    return this.http.get<CryptoQuote>(this.apiUrl + crypto.symbol + "/quote").pipe(
      map(reply => {
        let cryptoQuotes = Object.assign(new Array<CryptoQuote>(), reply);
        for (let cryptoQuote of cryptoQuotes) {
          cryptoQuote.quoteDate = new Date(cryptoQuote.quoteDate);
        }
        return cryptoQuotes;
      })
    );
  }
}
