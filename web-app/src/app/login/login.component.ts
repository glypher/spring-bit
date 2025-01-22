import { Component } from '@angular/core';
import { faGoogle, faFacebookF, faGithub } from '@fortawesome/free-brands-svg-icons';
import {FaIconComponent} from "@fortawesome/angular-fontawesome";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    FaIconComponent
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  // Icons for buttons
  googleIcon = faGoogle;
  facebookIcon = faFacebookF;
  githubIcon = faGithub;

  loginWithGitHub() {}

  loginWithGoogle() {}

  loginWithFacebook() {}
}
