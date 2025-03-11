import { Component } from '@angular/core';
import {MenuComponent} from "../menu/menu.component";
import {FormsModule} from "@angular/forms";
import {CryptoService} from "../service/crypto.service";
import {CryptoInfo, CryptoType, CryptoTypeImg} from "../service/service.types";
import {NgClass, NgForOf, NgIf, CommonModule} from "@angular/common";

@Component({
  selector: 'app-statistics',
  imports: [
    MenuComponent,
    FormsModule,
    NgForOf,
    NgIf,
    NgClass,
    CommonModule
  ],
  templateUrl: './statistics.component.html',
  styleUrl: './statistics.component.css'
})
export class StatisticsComponent {
  startDate: string = '2024-01-01';
  endDate: string = '2024-12-31';

  cryptoTypes: CryptoTypeImg[] = []

  selectedCryptos: string[] = [];
  cryptoInfo: CryptoInfo | null = null;

  constructor(private cryptoService: CryptoService) {
    cryptoService.cryptoTypes.subscribe(cti => this.cryptoTypes = cti);
  }


  toggleCrypto(symbol: string) {
    if (this.selectedCryptos.includes(symbol)) {
      this.selectedCryptos = this.selectedCryptos.filter(c => c !== symbol);
    } else {
      this.selectedCryptos.push(symbol);
    }
  }

  fetchInfo() {
    const startDate = new Date(this.startDate);
    const endDate = new Date(this.endDate);
    this.cryptoService.getCryptoInfo(this.cryptoTypes, startDate, endDate).subscribe(cryptoInfo => {
      this.cryptoInfo = cryptoInfo;
    });
  }

  getColor(value: number): string {
    if (value == 0) return  'bg-white'
    if (value >= 0.75) return 'bg-green-500';
    if (value >= 0.5) return 'bg-green-300';
    if (value >= 0.25) return 'bg-yellow-300';
    if (value > 0) return 'bg-gray-300';
    if (value >= -0.25) return 'bg-red-200';
    if (value >= -0.5) return 'bg-red-300';
    if (value >= -0.75) return 'bg-red-500';
    return 'bg-white';
  }
}
