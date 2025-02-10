import {Component, OnInit} from '@angular/core';
import {CommonModule} from "@angular/common";
import {CryptoService} from "../service/crypto.service";
import {CryptoType} from "../service/service.types";

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

  private fixNameLink(name: string) {
    name = name.toLowerCase();
    if (name == 'ripple')
      return 'xrp';
    if (name == 'polkadot')
      return 'polkadot-new';
    return name;
  }

  getAvailableCryptos(): void {
    this.cryptoService.loadCryptos().subscribe((data: CryptoType[]) => {
      this.cryptoIcons = data.map( (ct:CryptoType)=> {
        CryptoType.setType(ct);

        return {
          src: 'https://cryptologos.cc/logos/' + this.fixNameLink(ct.name) + '-' + ct.symbol.toLowerCase() + '-logo.svg',
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
