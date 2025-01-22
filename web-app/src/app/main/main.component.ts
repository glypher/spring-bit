import { Component } from '@angular/core';
import {GraphComponent} from "../graph/graph.component";
import {MenuComponent} from "../menu/menu.component";

@Component({
  selector: 'app-main',
  standalone: true,
    imports: [
        GraphComponent,
        MenuComponent
    ],
  templateUrl: './main.component.html',
  styleUrl: './main.component.css'
})
export class MainComponent {

}
