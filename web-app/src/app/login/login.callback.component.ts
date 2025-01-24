import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {AuthService} from "../service/auth.service";

@Component({
  selector: 'app-callback',
  template: `
    <p>Handling callback...</p>
  `
})
export class LoginCallbackComponent implements OnInit {

  constructor(private authService: AuthService, private router: Router) {
  }

  ngOnInit(): void {
    this.authService.loginSuccess();

    const redirectUrl = localStorage.getItem('returnUrlLogin') || '/';

    this.router.navigate([redirectUrl]);
  }


}
