import {Component, OnInit} from '@angular/core';
import {CryptoService} from "../service/crypto.service";
import {timer} from "rxjs";
import {environment} from "../../environments/environment";

@Component({
    selector: 'app-indicator',
    imports: [],
    templateUrl: './indicator.component.html',
    styleUrls: ['./indicator.component.css']
})
export class IndicatorComponent implements OnInit {
  isServiceAvailable: boolean = false;

  constructor(private cryptoService: CryptoService) {}

  ngOnInit(): void {
    this.cryptoService.isServiceAvailable.subscribe(
      status => {
        this.isServiceAvailable = status;

        if (!status) {
          timer(environment.liveTTL).subscribe(() => this.cryptoService.checkServiceStatus());
        }
      }
    )

    this.cryptoService.checkServiceStatus();
  }
}
