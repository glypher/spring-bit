import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';
import {BehaviorSubject, map, Observable} from 'rxjs';
import {Chat, CryptoInfo, CryptoQuote, CryptoType, CryptoTypeImg} from "./service.types";
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class CryptoService {
  private apiUrl = environment.serviceHost? `${window.location.protocol}//${environment.serviceHost}${environment.apiPath}` : environment.apiPath;
  private mlUrl = environment.mlHost? `${window.location.protocol}//${environment.mlHost}${environment.mlPath}` : environment.mlPath;

  private selectedSymbolSource = new BehaviorSubject<CryptoType>(CryptoType.DEFAULT_TYPE);
  selectedSymbol = this.selectedSymbolSource.asObservable();

  private cryptoTypesSource = new BehaviorSubject<CryptoTypeImg[]>([]);
  cryptoTypes = this.cryptoTypesSource.asObservable();

  private isServiceAvailableSource = new BehaviorSubject<boolean>(false);
  isServiceAvailable = this.isServiceAvailableSource.asObservable();

  constructor(private http: HttpClient) {}

  checkServiceStatus(): void {
    this.isAlive().subscribe({
      next: () => {
        this.isServiceAvailableSource.next(true);
        this.loadCryptos();
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

  loadCryptos(): void {
    function fixNameLink(name: string) {
      name = name.toLowerCase();
      if (name == 'ripple')
        return 'xrp';
      if (name == 'polkadot')
        return 'polkadot-new';
      return name;
    }

    this.http.get<CryptoType[]>(this.apiUrl + "cryptos").subscribe(
      types => {
        types.forEach(ct => CryptoType.setType(ct));

        let imgTypes = types.map(ct => ({
          name: ct.name,
          symbol: ct.symbol,
          src: 'https://cryptologos.cc/logos/' + fixNameLink(ct.name) + '-' + ct.symbol.toLowerCase() + '-logo.svg'
        }));

        this.cryptoTypesSource.next(imgTypes);
      });
  }

  getCryptoImgs() {
    return this.cryptoTypes;
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

  getCryptoInfo(cryptos: CryptoType[], start_date: Date, end_date: Date): Observable<CryptoInfo> {
    const cryptoInfoRequest = {
      start_date: start_date,
      end_date: end_date,
      symbols: cryptos.map(ct => ct.symbol)
    };

    return this.http.post<CryptoInfo>(this.mlUrl + "crypto/info", cryptoInfoRequest);
  }

  sendPrompt(prompt: string, history: {sender: string, text: string}[]) {
    const promptRequest = {
      prompt: prompt,
      history: history
    };
    return this.http.post<Chat>(this.mlUrl + "crypto/chat", promptRequest);
  }

}
