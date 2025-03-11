import { TestBed } from '@angular/core/testing';

import { CryptoService } from './crypto.service';
import { HttpTestingController, provideHttpClientTesting } from "@angular/common/http/testing";
import {environment} from "../../environments/environment";
import {CryptoType} from "./service.types";
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('CryptoService', () => {
  let service: CryptoService;
  let httpMock: HttpTestingController;
  let apiUrl: string = environment.apiPath;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [],
    providers: [CryptoService, provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
});

    service = TestBed.inject(CryptoService);
    httpMock = TestBed.inject(HttpTestingController);

    service.checkServiceStatus();

    httpMock.expectOne(apiUrl + 'live');
    httpMock.verify();
  });

  afterEach(() => {
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('is alive', () => {

    service.isAlive().subscribe((data) => {
      expect(data).toEqual("Server alive!");
    });

    const req = httpMock.expectOne(apiUrl + 'live');
    expect(req.request.method).toBe('GET');
    req.flush("Server alive!");
  });

  it('load cryptos', () => {

    service.checkServiceStatus();

    const req1 = httpMock.expectOne(apiUrl + 'live');
    expect(req1.request.method).toBe('GET');
    req1.flush("Server alive!");

    const req = httpMock.expectOne(apiUrl + 'cryptos');
    expect(req.request.method).toBe('GET');
    req.flush([new CryptoType("BITCOIN", "BTC"), new CryptoType("ETHEREUM", "ETH")]);

    service.cryptoTypes.subscribe((arr) => {
      let data : CryptoType[] = arr.map(d => new CryptoType(d.name, d.symbol));
      expect(data).toContain(new CryptoType("BITCOIN", "BTC"));
      expect(data).toContain(new CryptoType("ETHEREUM", "ETH"));
    });

  });

  it('get crypto data', () => {

    service.getCryptoData(new CryptoType("BITCOIN", "BTC")).subscribe((data) => {

      expect(data).toContain({
        name: "BITCOIN",
        symbol: "BTC",
        quoteDate: new Date("2024-01-10"),
        quotePrice: 1000});
    });

    const req = httpMock.expectOne(apiUrl + 'BTC/quote');
    expect(req.request.method).toBe('GET');
    req.flush([{
      name: "BITCOIN",
      symbol: "BTC",
      quoteDate: new Date("2024-01-10"),
      quotePrice: 1000}]);
  });
});
