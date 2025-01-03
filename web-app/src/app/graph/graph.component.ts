import { Component } from '@angular/core';
import {NgForOf} from "@angular/common";
import {CryptoQuote} from "../service/crypto.types";
import {CryptoService} from "../service/crypto.service";

@Component({
  selector: 'app-graph',
  standalone: true,
  imports: [
    NgForOf
  ],
  templateUrl: './graph.component.html',
  styleUrl: './graph.component.css'
})
export class GraphComponent {

  cryptoData: CryptoQuote[] = [];

  constructor(private cryptoService: CryptoService) {}

  ngOnInit(): void {
    this.cryptoService.selectedSymbol.subscribe(
      ct => {
        this.cryptoService.getCryptoData(ct).subscribe((data: CryptoQuote[]) => {
          this.cryptoData = data;
        });
      }
    )
  }
}
