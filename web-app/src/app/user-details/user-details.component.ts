import {Component, OnInit} from '@angular/core';
import {UserDetails} from "../service/service.types";
import {AuthService} from "../service/auth.service";
import {NgIf} from "@angular/common";
import {Router} from "@angular/router";

@Component({
  selector: 'app-user-details',
  imports: [
    NgIf
  ],
  templateUrl: './user-details.component.html',
  styleUrl: './user-details.component.css'
})
export class UserDetailsComponent implements OnInit {
  protected userDetails: UserDetails;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.authService.userDetails.subscribe(
      userDetails => this.userDetails = userDetails
    )
  }

  login() {
    this.router.navigate(['/login']);
  }

  logout() {
    this.authService.logout();

    this.router.navigate(['/']);
  }

  // Check if user is logged in or not
  get isLoggedIn(): boolean {
    return this.userDetails.username != "";
  }
}
