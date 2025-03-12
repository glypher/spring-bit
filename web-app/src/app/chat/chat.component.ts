import { Component } from '@angular/core';
import {CryptoService} from "../service/crypto.service";
import {FormsModule} from "@angular/forms";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-chat',
  imports: [
    FormsModule,
    NgIf
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent {
  prompt: string = '';
  response: string = '';
  loading: boolean = false;

  constructor(private cryptoService: CryptoService) {}

  sendPrompt() {
    this.loading = true;
    this.cryptoService.sendPrompt(this.prompt).subscribe({
      next: (chat) => {
        this.loading = false;
        this.response = chat.reply;
      },
      error: (error) => {
        this.loading = false;
        this.response = error.message;
      }
    });
  }
}
