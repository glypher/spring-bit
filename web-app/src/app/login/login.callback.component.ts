import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from "../service/auth.service";

@Component({
  selector: 'app-callback',
  template: `
    <p>Handling callback...</p>
  `
})
export class LoginCallbackComponent {

  constructor(private authService: AuthService, private router: Router, private route: ActivatedRoute) {
    this.route.queryParams.subscribe(params => {
      if ("error" in params) {
        this.authService.loginFailure(params['error'])
        this.router.navigate(['login']);
      } else {
        this.authService.loginSuccess();

        const redirectUrl = localStorage.getItem('returnUrlLogin') || '/';

        this.router.navigate([redirectUrl]);
      }
      console.log(params);  // { param1: "value1", param2: "value2" }
    });
  }

}
