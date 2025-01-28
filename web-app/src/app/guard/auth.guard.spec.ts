import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { authGuard } from './auth.guard';
import {provideHttpClient, withInterceptorsFromDi} from "@angular/common/http";
import {provideHttpClientTesting} from "@angular/common/http/testing";

describe('authGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
      TestBed.runInInjectionContext(() => authGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting()]
    });
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
