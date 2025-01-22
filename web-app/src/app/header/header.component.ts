import { Component } from '@angular/core';
import {IndicatorComponent} from "../indicator/indicator.component";

@Component({
  selector: 'app-header',
  imports: [
    IndicatorComponent
  ],
  templateUrl: './header.component.html',
  standalone: true,
  styleUrl: './header.component.css'
})
export class HeaderComponent {

}
