import { Component } from '@angular/core';
import {GraphComponent} from "../graph/graph.component";
import {MenuComponent} from "../menu/menu.component";
import {PortfolioComponent} from "../portfolio/portfolio.component";

@Component({
    selector: 'app-main',
  imports: [
    GraphComponent,
    MenuComponent,
    PortfolioComponent
  ],
    templateUrl: './main.component.html',
    styleUrl: './main.component.css'
})
export class MainComponent {

}
