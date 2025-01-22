import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {AuthConfig, OAuthService} from "angular-oauth2-oidc";
import {Router} from "@angular/router";

export enum AuthType {
  Github
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private userInfo: any;

  constructor(private oauthService: OAuthService, private router: Router) { }

  isLoggedIn() {
    return false;
  }

  login(provider: AuthType) {
    if (this.isLoggedIn())
      return;
    //AuthConfig

    if (provider === AuthType.Github) {
      this.oauthService.configure({
        clientId: environment.providers.githubAuth.clientId,
        redirectUri: environment.providers.githubAuth.redirectUri,
        scope: environment.providers.githubAuth.scope,
        responseType: 'code',  // Use 'code' response type for Authorization Code Flow
        strictDiscoveryDocumentValidation: false,
        showDebugInformation: true,
      });
    }

    this.oauthService.initCodeFlow();  // Start OAuth2 login process with PKCE
  }

  // Logout the user
  logout(): void {
    this.oauthService.logOut();
    this.router.navigate(['/']);
  }

  // Fetch the user profile information from GitHub
  loadUserProfile(): void {
    this.oauthService.loadUserProfile().then((profile) => {
      this.userInfo = profile;
      console.log('GitHub User Info:', profile);
    });
  }

  // Access the user info (username, avatar, etc.)
  getUserInfo(): any {
    return this.userInfo;
  }

  // Get the user's avatar URL (profile picture)
  getAvatarUrl(): string | null {
    return this.userInfo ? this.userInfo.avatar_url : null;
  }

  // Get the GitHub username
  getUserName(): string | null {
    return this.userInfo ? this.userInfo.login : null;
  }
}
