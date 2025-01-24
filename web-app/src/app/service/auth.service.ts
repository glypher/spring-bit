import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {AuthType, UserDetails} from "./service.types";
import {BehaviorSubject} from "rxjs";


@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private hasLoggedIn: boolean = false;

  private userDetailsSource = new BehaviorSubject<UserDetails>(UserDetails.DEFAULT_USER);
  userDetails = this.userDetailsSource.asObservable();

  constructor(private http: HttpClient) { }

  isLoggedIn() {
    return this.hasLoggedIn;
  }

  login(provider: AuthType) {
    this.hasLoggedIn = false;
    if (provider == AuthType.Github) {
      window.location.href = environment.serviceUrl + '/user/githubLogin';
      return;
    }
  }

  loginSuccess() {
    this.hasLoggedIn = true;
    this.http.get<UserDetails>(environment.serviceUrl + '/user/info').subscribe(
      userDetails => {
        this.userDetailsSource.next(userDetails);
        console.log(userDetails);
      }
    )
  }

  logout() {
    this.http.post(environment.serviceUrl + '/user/logout', {}).subscribe(
      userDetails => {
        this.hasLoggedIn = false;
        this.userDetailsSource.next(UserDetails.DEFAULT_USER);
        console.log(userDetails);
      });
  }
}
