import {AfterViewInit, Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';
import {CryptoAction, CryptoQuote} from "../service/service.types";
import {CryptoService} from "../service/crypto.service";
import {WebSocketService} from "../service/web-socket.service";
import { NgxChartsModule } from '@swimlane/ngx-charts';
import {User} from "../user/user";
import {Portfolio} from "../user/user.types";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-graph',
  imports: [
    NgxChartsModule,
    FormsModule
  ],
  templateUrl: './graph.component.html',
  standalone: true,
  styleUrl: './graph.component.css'
})
export class GraphComponent implements OnInit, AfterViewInit {

  cryptoData: CryptoQuote[] = [];
  lastData: CryptoQuote;
  isWsAvailable = false;

  game = {
    win: 100000,
    loose: 30,
    state: 0, // 0 - init, 1-buy, 2-sell, 3 - lost, 4 - won
    countOps: 0,
    buyQuantity: 0,
    sellQuantity: 0,
    infoText: ''
  };

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

          this.game.state = 0;
          this.game.infoText = '';
          this.game.countOps = 0;

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
      if (this.game.countOps) {
        this.game.countOps -= 1;
        if (this.game.countOps == 0) {
          this.portfolioAction();
        } else {
          this.game.infoText = (this.game.state==1? 'Buying in ' : 'Selling in ') + this.game.countOps + ' steps';
        }
      }
      this.addCryptoToGraph(cryptoQuote);
    });

    this.user.portfolio.subscribe(portfolio => {
      // update according to portfolio change

      let usd = portfolio.getMoney();
      this.game.buyQuantity = usd;
      this.game.sellQuantity = portfolio.getStock(this.cryptoData[0].symbol || '');

      if (usd >= this.game.win) {
        this.game.state = 4;
        this.game.infoText = 'You won! Congrats.';
      } else if ((usd < this.game.loose) && !portfolio.hasCrypto()) {
        this.game.state = 3;
        this.game.infoText = "You lost! Don't buy crypto.";
      } else {
        this.game.state = 0;
        this.game.infoText = '';
      }
    });

    this.onResize();
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.view = [this.graphDiv.nativeElement.offsetWidth , Math.min(window.innerHeight / 3, 700)];
  }

  doAction(buy: boolean) {
    this.game.state = buy? 1 : 2;
    this.game.countOps = this.game.countOps <= 0? 3 : this.game.countOps;
    this.game.infoText = (this.game.state==1? 'Buying in ' : 'Selling in ') + this.game.countOps + ' steps';

    this.colorScheme = 'vivid';
  }

  tryAgain() {
    this.game.state = 0;
    this.user.resetPortfolio();
  }

  private portfolioAction() {

    let action = Object.assign(new CryptoAction(),
      {...this.lastData, operation: this.game.state==1? 'Buy' : 'Sell',
        quantity: this.game.state == 1? this.game.buyQuantity : this.game.sellQuantity});

    this.colorScheme = 'cool';

    // First update portfolio to check availability
    action = this.user.addCryptoAction(action);
    if (action.quantity > 0) {
      this.webSocketService.sendMessage(action);
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
