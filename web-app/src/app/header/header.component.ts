import { Component } from '@angular/core';
import {IndicatorComponent} from "../indicator/indicator.component";

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    IndicatorComponent
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

}
