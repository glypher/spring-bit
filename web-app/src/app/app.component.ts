import {Component} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router, RouterOutlet} from '@angular/router';
import {CommonModule} from "@angular/common";
import {HeaderComponent} from "./header/header.component";
import {FooterComponent} from "./footer/footer.component";
import {AuthService} from "./service/auth.service";
import {filter} from "rxjs";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, HeaderComponent, FooterComponent],
  templateUrl: './app.component.html',
  standalone: true,
  styleUrl: './app.component.css'
})

export class AppComponent {
  title = 'Springbit webapp';
  private nextUrl: string = '/';

  constructor(private authService: AuthService, private router: Router, private route: ActivatedRoute) {
    this.route.queryParams.subscribe(params => {
      if ("loginError" in params) {
        this.authService.loginFailure(params['loginError'])
        this.nextUrl = 'login';
        this.router.navigate(['/']);
      } else if ("login" in params) {
        this.authService.loginSuccess();

        let redirectUrl = localStorage.getItem('returnUrlLogin');
        if (redirectUrl) {
          localStorage.removeItem('returnUrlLogin');
          this.nextUrl = redirectUrl;
        }
        this.router.navigate(['/']);
      }
    });

    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        if (this.nextUrl != '/') {
          this.router.navigate([this.nextUrl], { skipLocationChange: true });
          this.nextUrl = '/';
        }
      });
  }
}
