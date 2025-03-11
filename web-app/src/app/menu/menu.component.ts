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
    this.cryptoService.cryptoTypes.subscribe(ct => this.cryptoIcons = ct);
  }

  setSymbol(symbol: string): void {
    this.cryptoService.onSymbolChange(CryptoType.getType(symbol));
  }

}
