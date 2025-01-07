import { TestBed } from '@angular/core/testing';

import { CryptoService } from './crypto.service';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {environment} from "../../environments/environment";
import {CryptoQuote, CryptoType} from "./crypto.types";
import {map, Observable} from "rxjs";

describe('CryptoService', () => {
  let service: CryptoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule], // Import the HttpClientTestingModule
      providers: [CryptoService]
    });

    service = TestBed.inject(CryptoService);
    httpMock = TestBed.inject(HttpTestingController);

    httpMock.expectOne(environment.apiUrl + 'live');
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

    const req = httpMock.expectOne(environment.apiUrl + 'live');
    expect(req.request.method).toBe('GET');
    req.flush("Server alive!");
  });

  it('load cryptos', () => {

    service.loadCryptos().subscribe((data) => {
      expect(data).toContain(new CryptoType("BITCOIN", "BTC"));
      expect(data).toContain(new CryptoType("ETHEREUM", "ETH"));
    });

    const req = httpMock.expectOne(environment.apiUrl + 'cryptos');
    expect(req.request.method).toBe('GET');
    req.flush([new CryptoType("BITCOIN", "BTC"), new CryptoType("ETHEREUM", "ETH")]);
  });

  it('get crypto data', () => {

    service.getCryptoData(new CryptoType("BITCOIN", "BTC")).subscribe((data) => {

      expect(data).toContain({
        name: "BITCOIN",
        symbol: "BTC",
        quoteDate: new Date("2024-01-10"),
        quotePrice: 1000});
    });

    const req = httpMock.expectOne(environment.apiUrl + 'BTC/quote');
    expect(req.request.method).toBe('GET');
    req.flush([{
      name: "BITCOIN",
      symbol: "BTC",
      quoteDate: new Date("2024-01-10"),
      quotePrice: 1000}]);
  });
});
