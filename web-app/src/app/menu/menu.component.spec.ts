import {ComponentFixture, TestBed} from '@angular/core/testing';

import { MenuComponent } from './menu.component';
import {CryptoService} from "../service/crypto.service";
import {BehaviorSubject, of} from "rxjs";
import {CryptoType, CryptoTypeImg} from "../service/service.types";

describe('MenuComponent', () => {
  let component: MenuComponent;
  let fixture: ComponentFixture<MenuComponent>;
  let mockCryptoService: Partial<CryptoService>;
  let mockCryptoTypes = new BehaviorSubject<CryptoTypeImg[]>([]);

  beforeEach(async () => {
    // Mock the BehaviorSubject to simulate observable changes
    mockCryptoService = {
      cryptoTypes: mockCryptoTypes.asObservable(),
      isAlive: jasmine.createSpy('isAlive').and.callFake(() => { return "Server alive!" }),
      loadCryptos:  jasmine.createSpy('loadCryptos').and.callFake(() => {
        mockCryptoTypes.next([new CryptoTypeImg("BITCOIN", "BTC"), new CryptoTypeImg("ETHEREUM", "ETH")]);
      }),
      onSymbolChange: jasmine.createSpy('onSymbolChange')
    };

    await TestBed.configureTestingModule({
      imports: [MenuComponent],
      providers: [{ provide: CryptoService, useValue: mockCryptoService }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MenuComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should should load icons', () => {

    const compiled = fixture.nativeElement as HTMLElement; // Access the native DOM
    let imgs = compiled.querySelectorAll('img');
    expect(imgs).toHaveSize(0);

    mockCryptoTypes.next([new CryptoTypeImg("BITCOIN", "BTC"), new CryptoTypeImg("ETHEREUM", "ETH")]);
    fixture.detectChanges();

    imgs = compiled.querySelectorAll('img');
    expect(imgs).toHaveSize(2);
    expect(imgs[0].alt).toEqual("BITCOIN");
    expect(imgs[1].alt).toEqual("ETHEREUM");

    imgs[0].click();
    expect(mockCryptoService.onSymbolChange).toHaveBeenCalledWith(new CryptoType("BITCOIN", "BTC"));
  });
});
