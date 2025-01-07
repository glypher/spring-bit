import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {BehaviorSubject} from 'rxjs';

import { IndicatorComponent } from './indicator.component';
import {CryptoService} from "../service/crypto.service";

describe('IndicatorComponent', () => {
  let component: IndicatorComponent;
  let fixture: ComponentFixture<IndicatorComponent>;
  let mockCryptoService: Partial<CryptoService>;
  let mockIsAlive = new BehaviorSubject<boolean>(false);

  beforeEach(async () => {
    // Mock the BehaviorSubject to simulate observable changes
    mockCryptoService = {
      isServiceAvailable: mockIsAlive.asObservable()
    };

    await TestBed.configureTestingModule({
      imports: [IndicatorComponent],
      providers: [{ provide: CryptoService, useValue: mockCryptoService }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IndicatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have indicator change', fakeAsync(() => {

    const compiled = fixture.nativeElement as HTMLElement; // Access the native DOM
    const indicator = compiled.querySelector('.rounded-full');
    expect(indicator).toHaveClass('bg-red-500');

    setTimeout(() => {
      mockIsAlive.next(true);
    }, 4000);

    tick(4000);

    fixture.detectChanges();
    mockIsAlive.asObservable().subscribe((done) => {
      expect(done).toEqual(true);

      expect(indicator).toHaveClass('bg-green-500');
    })

  }));
});
