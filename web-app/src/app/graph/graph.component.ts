import {AfterViewInit, Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';
import {CryptoAction, CryptoQuote} from "../service/service.types";
import {CryptoService} from "../service/crypto.service";
import {WebSocketService} from "../service/web-socket.service";
import { NgxChartsModule } from '@swimlane/ngx-charts';
import {User} from "../user/user";
import {Portfolio} from "../user/user.types";

@Component({
  selector: 'app-graph',
  imports: [
    NgxChartsModule
  ],
  templateUrl: './graph.component.html',
  standalone: true,
  styleUrl: './graph.component.css'
})
export class GraphComponent implements OnInit, AfterViewInit {

  cryptoData: CryptoQuote[] = [];
  lastData: CryptoQuote;
  lastOperation: string = 'Buy';
  countOps: number = 0;
  isWsAvailable = false;

  game = {win: 100000, loose: 30.0};

  @ViewChild('graphDiv', { static: true }) graphDiv!: ElementRef;

  data: { name: string; series: { name: string; value: number }[] }[] = [
    { name: 'Future Crypto Data', series: [] }
  ];
  view: [number, number] = [Math.min(window.innerWidth/ 3, 700), Math.min(window.innerHeight / 4, 700)];
  colorScheme = 'cool';

  constructor(private cryptoService: CryptoService, private webSocketService: WebSocketService, private user: User) {}

  ngAfterViewInit(): void {
    this.graphDiv.nativeElement.focus();
  }

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

          this.lastOperation = 'Buy';
          this.countOps = 0;

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

    this.user.portfolio.subscribe(portfolio => {
      this.nextLastOp(portfolio);
    });

    this.onResize();
  }


  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent): void {
    if (event.code === 'Space' || event.key === ' ') {
      this.doAction();
      event.preventDefault();
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.view = [this.graphDiv.nativeElement.offsetWidth , Math.min(window.innerHeight / 3, 700)];
  }

  doAction() {
    let reset = this.lastOperation == 'Won' || this.lastOperation == 'Try Again';
    if (reset)
      this.user.resetPortfolio();
    else {
      this.countOps = this.countOps <= 0? 3 : this.countOps;
    }
    this.nextLastOp(null);

    this.colorScheme = 'vivid';
  }

  private portfolioAction() {
    let op = ''
    if (this.lastOperation == 'Buying')
      op = 'Buy';
    if (this.lastOperation == 'Selling')
      op = 'Sell';
    this.nextLastOp(null);

    let action = Object.assign(new CryptoAction(),
      {...this.lastData, operation: op, quantity: 100000});

    this.colorScheme = 'cool';

    // First update portfolio to check availability
    action = this.user.addCryptoAction(action);
    if (action.quantity > 0) {
      this.webSocketService.sendMessage(action);
    }
  }

  private nextLastOp(portfolio: Portfolio | null) {
    if (portfolio) {
      let usd = portfolio.getMoney();
      if (usd >= this.game.win) {
        this.lastOperation = 'Won';
      } else if ((usd < this.game.loose) && !portfolio.hasCrypto()) {
        this.lastOperation = 'Try Again';
      }
      return;
    }
    switch (this.lastOperation) {
      case 'Buy': this.lastOperation = 'Buying'; break;
      case 'Buying': this.lastOperation = 'Sell'; break;
      case 'Sell': this.lastOperation = 'Selling'; break;
      case 'Selling':
      case 'Won':
      case 'Try Again': this.lastOperation = 'Buy'; break;
    }
  }

  private addCryptoToGraph(cryptoQuote: CryptoQuote) {
    this.lastData = cryptoQuote;
    if (this.data[0].series.length >= 20) {
      this.data[0].series.shift(); // Keep a fixed number of data points
    }
    this.data[0].series.push({
      name: cryptoQuote.quoteDate.toLocaleTimeString([], {
        minute: '2-digit',
        second: '2-digit',
      }),
      value: cryptoQuote.quotePrice.valueOf()});
    this.data = [...this.data]; // Refresh the chart
  }

}
