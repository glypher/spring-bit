import { Component } from '@angular/core';
import { faGoogle, faFacebookF, faGithub } from '@fortawesome/free-brands-svg-icons';
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {AuthType, AuthService} from "../service/auth.service";

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
  // Icons for buttons
  googleIcon = faGoogle;
  facebookIcon = faFacebookF;
  githubIcon = faGithub;

  constructor(private authService: AuthService) {
  }

  loginWithGitHub() {
      this.authService.login(AuthType.Github);
  }
}
