import {Component, OnInit} from '@angular/core';
import {NgForOf} from "@angular/common";
import {CryptoQuote} from "../service/service.types";
import {CryptoService} from "../service/crypto.service";

@Component({
  selector: 'app-graph',
  imports: [
    NgForOf
  ],
  templateUrl: './graph.component.html',
  standalone: true,
  styleUrl: './graph.component.css'
})
export class GraphComponent implements OnInit {

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
