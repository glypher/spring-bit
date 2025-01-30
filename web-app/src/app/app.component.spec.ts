import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { provideHttpClient } from "@angular/common/http";
import {provideRouter, Router} from "@angular/router";
import {MainComponent} from "./main/main.component";
import {provideAnimationsAsync} from "@angular/platform-browser/animations/async";

describe('AppComponent', () => {
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideHttpClient(),
        provideRouter([
          { path: '', component: MainComponent },
          { path: '**', redirectTo: ''}
        ]),
        provideAnimationsAsync()
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    await router.navigate(['']);
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have the 'web-app' title`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('Springbit webapp');
  });
});
