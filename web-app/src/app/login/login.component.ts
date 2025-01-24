import {Component} from '@angular/core';
import {faGithub} from '@fortawesome/free-brands-svg-icons';
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {AuthService} from "../service/auth.service";
import {AuthType} from "../service/service.types";

@Component({
    selector: 'app-login',
    imports: [
        FaIconComponent
    ],
    templateUrl: './login.component.html',
    standalone: true,
    styleUrl: './login.component.css'
})
export class LoginComponent {
  protected readonly githubIcon = faGithub;

  constructor(private authService: AuthService) {
  }

  loginWithGitHub() {
    this.authService.login(AuthType.Github);
  }

}
