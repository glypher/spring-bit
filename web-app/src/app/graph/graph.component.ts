import {Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';
import {NgForOf} from "@angular/common";
import {CryptoAction, CryptoQuote} from "../service/service.types";
import {CryptoService} from "../service/crypto.service";
import {WebSocketService} from "../service/web-socket.service";
import { NgxChartsModule } from '@swimlane/ngx-charts';
import {User} from "../user/user";

@Component({
  selector: 'app-graph',
  imports: [
    NgForOf,
    NgxChartsModule
  ],
  templateUrl: './graph.component.html',
  standalone: true,
  styleUrl: './graph.component.css'
})
export class GraphComponent implements OnInit {

  cryptoData: CryptoQuote[] = [];
  lastData: CryptoQuote;
  lastOperation: string = 'buy';
  isWsAvailable = false;

  @ViewChild('graphDiv', { static: true }) graphDiv!: ElementRef;

  data: { name: string; series: { name: string; value: number }[] }[] = [
    { name: 'Future Crypto Data', series: [] }
  ];
  view: [number, number] = [700, 400];
  colorScheme = 'cool';

  constructor(private cryptoService: CryptoService, private webSocketService: WebSocketService, private user: User) {}

  ngOnInit(): void {
    this.cryptoService.selectedSymbol.subscribe(
      ct => {
        this.cryptoService.getCryptoData(ct).subscribe((data: CryptoQuote[]) => {
          this.cryptoData = data;

          if (!this.isWsAvailable)
            this.webSocketService.connect();
        });
      }
    );

    this.webSocketService.isServiceAvailable.subscribe(connected => {
      this.isWsAvailable = connected;
      if (this.isWsAvailable) {
        // need to let server know that prediction must start for current symbol
        let action = Object.assign(new CryptoAction(),
          {...this.cryptoData[0], operation: 'predict', quantity: 50000.0});

        this.webSocketService.sendMessage(action);
      }
    });

    this.webSocketService.cryptoQuote.subscribe(cryptoQuote  => {
      this.lastData = cryptoQuote;
      if (this.data[0].series.length >= 40) {
        this.data[0].series.shift(); // Keep a fixed number of data points
      }
      this.data[0].series.push({
        name: cryptoQuote.quoteDate.toLocaleTimeString(),
        value: cryptoQuote.quotePrice.valueOf()});
      this.data = [...this.data]; // Refresh the chart
    });

    this.user.portfolio.subscribe(profolio => {

    });

    this.onResize();
  }


  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent): void {
    if (event.code === 'Space' || event.key === ' ') {
      let action = Object.assign(new CryptoAction(),
        {...this.lastData, operation: this.lastOperation, quantity: 100});

      // First update portfolio to check availability
      action = this.user.addCryptoAction(action);
      if (action.quantity > 0) {
        this.colorScheme = this.lastOperation == 'buy'? 'vivid' : 'cool';
          this.lastOperation = this.lastOperation == 'buy'? 'sell' : 'buy';
        this.webSocketService.sendMessage(action);
      }
      event.preventDefault();
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.view = [this.graphDiv.nativeElement.offsetWidth , 700];
  }
}
