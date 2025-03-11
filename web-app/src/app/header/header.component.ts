import { Component } from '@angular/core';
import {IndicatorComponent} from "../indicator/indicator.component";
import {UserDetailsComponent} from "../user-details/user-details.component";
import {NgOptimizedImage} from "@angular/common";
import {Router} from "@angular/router";

@Component({
  selector: 'app-header',
  imports: [
    IndicatorComponent,
    UserDetailsComponent,
    NgOptimizedImage
  ],
  templateUrl: './header.component.html',
  standalone: true,
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  constructor(private router: Router) {
  }
  onHome() {
    this.router.navigate(["/"], { skipLocationChange: true });
  }

  onGame() {
    this.router.navigate(["game"], { skipLocationChange: true });
  }

  onInfo() {
    this.router.navigate(["info"], { skipLocationChange: true });
  }

}
