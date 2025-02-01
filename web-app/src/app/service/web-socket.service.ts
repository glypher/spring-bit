import { Injectable } from '@angular/core';
import {CryptoQuote} from "./service.types";
import {environment} from "../../environments/environment";
import {BehaviorSubject, map, Subject, timer} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private cryptoQuoteSource = new Subject<CryptoQuote>();
  cryptoQuote = this.cryptoQuoteSource.asObservable();

  private isServiceAvailableSource = new BehaviorSubject<boolean>(false);
  isServiceAvailable = this.isServiceAvailableSource.asObservable();

  private socket: WebSocket;

  connect() {
    if (this.socket) {
      this.socket.close(); // Ensure no duplicate connections
    }

    const wsProto = window.location.protocol == 'https:'? 'wss:' : 'ws:';
    const url = `${wsProto}//${environment.serviceHost}${environment.wsPath}`

    this.socket = new WebSocket(url);

    this.socket.onopen = () => {
      console.log('WebSocket connected');
      this.isServiceAvailableSource.next(true);
    };

    this.socket.onmessage = (event) => {
      console.log('Message received:', event.data);

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
      timer(environment.liveTTL).subscribe(() => this.connect());
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
    }
  }
}
