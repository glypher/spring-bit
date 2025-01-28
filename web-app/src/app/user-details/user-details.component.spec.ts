import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserDetailsComponent } from './user-details.component';
import {provideHttpClient, withInterceptorsFromDi} from "@angular/common/http";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideRouter} from "@angular/router";

describe('UserDetailsComponent', () => {
  let component: UserDetailsComponent;
  let fixture: ComponentFixture<UserDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserDetailsComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
