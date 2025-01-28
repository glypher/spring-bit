import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MainComponent } from './main.component';
import {provideHttpClient, withInterceptorsFromDi} from "@angular/common/http";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideRouter} from "@angular/router";

describe('MainComponent', () => {
  let component: MainComponent;
  let fixture: ComponentFixture<MainComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MainComponent],
      providers: [provideHttpClient(withInterceptorsFromDi()), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
