import {Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';
import {NgForOf} from "@angular/common";
import {CryptoAction, CryptoQuote} from "../service/service.types";
import {CryptoService} from "../service/crypto.service";
import {WebSocketService} from "../service/web-socket.service";
import { NgxChartsModule } from '@swimlane/ngx-charts';
import {User} from "../user/user";
import {timer} from "rxjs";
import {environment} from "../../environments/environment";

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
  countOps: number = 0;
  isWsAvailable = false;

  @ViewChild('graphDiv', { static: true }) graphDiv!: ElementRef;

  data: { name: string; series: { name: string; value: number }[] }[] = [
    { name: 'Future Crypto Data', series: [] }
  ];
  view: [number, number] = [700, 400];
  colorScheme = 'cool';

  constructor(private cryptoService: CryptoService, private webSocketService: WebSocketService, private user: User) {}

  ngOnInit(): void {

    this.cryptoService.isServiceAvailable.subscribe(connected => {
      if (connected && this.cryptoData.length > 0) {
        this.webSocketService.connect();
      }
    });

    this.cryptoService.selectedSymbol.subscribe(
      ct => {
        this.cryptoService.getCryptoData(ct).subscribe((data: CryptoQuote[]) => {
          this.cryptoData = data;
          // reinitialize the graph
          this.data[0].name = this.cryptoData[0].name;
          this.data[0].series = [];
          this.addCryptoToGraph(this.cryptoData[0]);

          if (!this.isWsAvailable) {
            this.webSocketService.connect();
          } else {
            // need to let server know that prediction must start for current symbol
            let action = Object.assign(new CryptoAction(),
              {...this.cryptoData[0], operation: 'predict', quantity: 50000.0});

            this.webSocketService.sendMessage(action);
          }
        });
      }
    );

    this.webSocketService.isServiceAvailable.subscribe(connected => {
      this.isWsAvailable = connected;
      if (connected) {
        // need to let server know that prediction must start for current symbol
        let action = Object.assign(new CryptoAction(),
          {...this.cryptoData[0], operation: 'predict', quantity: 50000.0});

        this.webSocketService.sendMessage(action);
      } else {
        this.cryptoService.checkServiceStatus();
      }
    });

    this.webSocketService.cryptoQuote.subscribe(cryptoQuote  => {
      if (this.countOps) {
        this.countOps -= 1;
        if (this.countOps == 0) {
          this.portfolioAction();
        }
      }
      this.addCryptoToGraph(cryptoQuote);
    });

    this.onResize();
  }


  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent): void {
    if (event.code === 'Space' || event.key === ' ') {
      this.countOps = this.countOps <= 0? 3 : this.countOps;

      event.preventDefault();
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.view = [this.graphDiv.nativeElement.offsetWidth , 700];
  }

  private portfolioAction() {
    let action = Object.assign(new CryptoAction(),
      {...this.lastData, operation: this.lastOperation, quantity: 100});

    // First update portfolio to check availability
    action = this.user.addCryptoAction(action);
    if (action.quantity > 0) {
      this.colorScheme = this.lastOperation == 'buy'? 'vivid' : 'cool';
      this.lastOperation = this.lastOperation == 'buy'? 'sell' : 'buy';

      this.webSocketService.sendMessage(action);
    }
  }

  private addCryptoToGraph(cryptoQuote: CryptoQuote) {
    this.lastData = cryptoQuote;
    if (this.data[0].series.length >= 40) {
      this.data[0].series.shift(); // Keep a fixed number of data points
    }
    this.data[0].series.push({
      name: cryptoQuote.quoteDate.toLocaleTimeString(),
      value: cryptoQuote.quotePrice.valueOf()});
    this.data = [...this.data]; // Refresh the chart
  }
}
