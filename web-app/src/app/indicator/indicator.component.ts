import {Component, OnInit} from '@angular/core';
import {CryptoService} from "../service/crypto.service";

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
      status => this.isServiceAvailable = status
    )
  }
}
