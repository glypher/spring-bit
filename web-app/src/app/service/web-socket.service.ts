import { Injectable } from '@angular/core';
import {CryptoQuote} from "./service.types";
import {environment} from "../../environments/environment";
import {BehaviorSubject, Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private cryptoQuoteSource = new Subject<CryptoQuote>();
  cryptoQuote = this.cryptoQuoteSource.asObservable();

  private isServiceAvailableSource = new BehaviorSubject<boolean>(false);
  isServiceAvailable = this.isServiceAvailableSource.asObservable();

  private socket: WebSocket  | null = null;

  connect() {
    if (this.socket) {
      return;
    }

    const wsProto = window.location.protocol == 'https:'? 'wss:' : 'ws:';
    const wsHost = environment.serviceHost? environment.serviceHost : window.location.host;
    const url = `${wsProto}//${wsHost}${environment.wsPath}`

    this.socket = new WebSocket(url);

    this.socket.onopen = () => {
      this.isServiceAvailableSource.next(true);
    };

    this.socket.onmessage = (event) => {
      let cryptoQuote = Object.assign(new CryptoQuote, JSON.parse(event.data));
      if (cryptoQuote) {
        cryptoQuote.quoteDate = new Date(cryptoQuote.quoteDate);
        this.cryptoQuoteSource.next(cryptoQuote);
      }
    };

    this.socket.onerror = (error) => {
      //console.error('WebSocket error:', error);
    };

    this.socket.onclose = (event) => {
      //console.log('WebSocket closed', event.reason);
      this.isServiceAvailableSource.next(false);
    };

  }

  sendMessage(message: any): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN && message) {
      this.socket.send(JSON.stringify(message));
    } else {
      console.warn('WebSocket is not open. Message not sent.');
    }
  }

  close(): void {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
  }
}
