import {Component, OnInit} from '@angular/core';
import {CommonModule} from "@angular/common";
import {CryptoService} from "../service/crypto.service";
import {CryptoType} from "../service/crypto.types";

@Component({
  selector: 'app-menu',
  imports: [CommonModule],
  templateUrl: './menu.component.html',
  standalone: true,
  styleUrl: './menu.component.css'
})
export class MenuComponent implements OnInit {
  cryptoIcons: any = [];

  constructor(private cryptoService: CryptoService) {
  }

  ngOnInit() {
    this.cryptoService.isServiceAvailable.subscribe(
      status => status? this.getAvailableCryptos() : this.cryptoIcons = []
    )
  }

  getAvailableCryptos(): void {
    this.cryptoService.loadCryptos().subscribe((data: CryptoType[]) => {
      this.cryptoIcons = data.map( (ct:CryptoType)=> {
        CryptoType.setType(ct);

        return {
          src: 'https://cryptologos.cc/logos/' + ct.name.toLowerCase() + '-' + ct.symbol.toLowerCase() + '-logo.svg?v=025',
          alt: ct.name,
          symbol: ct.symbol
        }
      });
    });
  }

  setSymbol(symbol: string): void {
    this.cryptoService.onSymbolChange(CryptoType.getType(symbol));
  }

}
