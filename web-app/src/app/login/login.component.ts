import {Component} from '@angular/core';
import {faGithub, faFacebook} from '@fortawesome/free-brands-svg-icons';
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {AuthService} from "../service/auth.service";
import {AuthType} from "../service/service.types";
import {NgIf} from "@angular/common";

@Component({
    selector: 'app-login',
  imports: [
    FaIconComponent,
    NgIf,
  ],
    templateUrl: './login.component.html',
    standalone: true,
    styleUrl: './login.component.css'
})
export class LoginComponent {
  protected readonly githubIcon = faGithub;
  protected readonly facebookIcon = faFacebook;
  protected loginError: string;

  constructor(private authService: AuthService) {
    this.authService.loginError.subscribe(error => this.loginError = error);
  }

  loginWithGitHub() {
    this.authService.login(AuthType.Github);
  }

  loginWithFacebook() {
    this.authService.login(AuthType.Facebook);
  }

  loginWithKeycloak() {
    this.authService.login(AuthType.Keycloak);
  }
}
