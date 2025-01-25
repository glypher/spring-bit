import { Component } from '@angular/core';
import {IndicatorComponent} from "../indicator/indicator.component";
import {UserDetailsComponent} from "../user-details/user-details.component";
import {NgOptimizedImage} from "@angular/common";
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-header',
  imports: [
    IndicatorComponent,
    UserDetailsComponent,
    NgOptimizedImage,
    RouterLink
  ],
  templateUrl: './header.component.html',
  standalone: true,
  styleUrl: './header.component.css'
})
export class HeaderComponent {

}
