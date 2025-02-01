import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {AuthType, UserDetails} from "./service.types";
import {BehaviorSubject} from "rxjs";


@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private hasLoggedIn: boolean = false;

  private authUrl = environment.serviceHost? `${window.location.protocol}//${environment.serviceHost}` : ''

  private userDetailsSource = new BehaviorSubject<UserDetails>(UserDetails.DEFAULT_USER);
  userDetails = this.userDetailsSource.asObservable();

  private loginErrorSource = new BehaviorSubject<string>("");
  loginError = this.loginErrorSource.asObservable();

  constructor(private http: HttpClient) { }

  isLoggedIn() {
    return this.hasLoggedIn;
  }

  login(provider: AuthType) {
    this.hasLoggedIn = false;
    switch (provider) {
      case AuthType.Github:   window.location.href = this.authUrl + '/user/githubLogin';   break;
      case AuthType.Facebook: window.location.href = this.authUrl + '/user/facebookLogin'; break;
      case AuthType.Keycloak: window.location.href = this.authUrl + '/user/keycloakLogin'; break;
    }
  }

  loginSuccess() {
    this.hasLoggedIn = true;
    this.http.get<UserDetails>(this.authUrl + '/user/info').subscribe(
      userDetails => {
        this.userDetailsSource.next(userDetails);
        console.log(userDetails);
      }
    )
  }

  loginFailure(error: string) {
    this.loginErrorSource.next(error);
  }

  logout() {
    this.http.post(this.authUrl + '/user/logout', {}).subscribe(
      userDetails => {
        this.hasLoggedIn = false;
        this.userDetailsSource.next(UserDetails.DEFAULT_USER);
        console.log(userDetails);
      });
  }
}
