import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraphComponent } from './graph.component';
import {CryptoService} from "../service/crypto.service";
import {BehaviorSubject, of} from "rxjs";
import {CryptoQuote, CryptoType} from "../service/service.types";

describe('GraphComponent', () => {
  let component: GraphComponent;
  let fixture: ComponentFixture<GraphComponent>;
  let mockCryptoService: Partial<CryptoService>;
  let mockSelSymbol = new BehaviorSubject<CryptoType>(new CryptoType("BITCOIN", "BTC"));

  beforeEach(async () => {
    // Mock the BehaviorSubject to simulate observable changes
    mockCryptoService = {
      selectedSymbol: mockSelSymbol.asObservable(),
      getCryptoData:  jasmine.createSpy('getCryptoData').and.callFake((cryptoType) => {
        return of([{
          name: cryptoType.name,
          symbol: cryptoType.symbol,
          quoteDate: new Date(),
          quotePrice: 1000}]);
      })
    };

    await TestBed.configureTestingModule({
      imports: [GraphComponent],
      providers: [{ provide: CryptoService, useValue: mockCryptoService }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get symbol quote', () => {
    expect(component).toBeTruthy();
  });

  it('should should load icons', () => {
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement; // Access the native DOM
    let tags = compiled.querySelectorAll('p');
    expect(tags).toHaveSize(2);
    expect(tags[0].innerText).toEqual("Symbol: BTC");
    expect(tags[1].innerText).toEqual("Price: $1000");

    mockSelSymbol.next(new CryptoType("ETHEREUM", "ETH"));
    fixture.detectChanges();

    tags = compiled.querySelectorAll('p');
    expect(tags[0].innerText).toEqual("Symbol: ETH");
    expect(tags[1].innerText).toEqual("Price: $1000");
  });
});
