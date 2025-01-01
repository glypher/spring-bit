import {Component, OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {CryptoService} from "./crypto.service";
import {CommonModule} from "@angular/common";
import {HttpClientModule} from "@angular/common/http";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, HttpClientModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})

export class AppComponent implements OnInit {
  title = 'web-app';

  cryptoData: any[] = [];

  constructor(private cryptoService: CryptoService) {}

  ngOnInit(): void {
    this.cryptoService.getCryptoData("BTC").subscribe((data) => {
      this.cryptoData = data;
    });
  }
}
