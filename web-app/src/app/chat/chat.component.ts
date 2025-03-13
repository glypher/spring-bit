import {Component, ElementRef, ViewChild} from '@angular/core';
import {CryptoService} from "../service/crypto.service";
import {FormsModule} from "@angular/forms";
import {NgClass, NgForOf, NgIf} from "@angular/common";
import {animate, style, transition, trigger} from "@angular/animations";

@Component({
  selector: 'app-chat',
  imports: [
    FormsModule,
    NgClass,
    NgForOf
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css',
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(10px)' }),
        animate('300ms ease-out', style({ opacity: 1, transform: 'translateY(0)' })),
      ]),
    ]),
  ],
})
export class ChatComponent {
  @ViewChild('chatContainer') chatContainer!: ElementRef;

  prompt: string = '';
  loading: boolean = false;
  history: {sender: string, text: string}[] = []

  constructor(private cryptoService: CryptoService) {}

  sendPrompt() {
    if (!this.prompt.trim()) return;

    this.loading = true;
    this.history.push({ sender: 'user', text: this.prompt });
    this.scrollToBottom();

    this.cryptoService.sendPrompt(this.prompt, this.history).subscribe({
      next: (chat) => {
        this.loading = false;
        this.history.push({ sender: 'ai', text: chat.reply });
        this.scrollToBottom();
      },
      error: (error) => {
        this.loading = false;
        this.history.push({ sender: 'error', text: error.message });
      }
    });
  }

  scrollToBottom() {
    setTimeout(() => {
      if (this.chatContainer) {
        this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
      }
    }, 100);
  }

  checkScrollHeight() {
    if (!this.chatContainer) return;

    const container = this.chatContainer.nativeElement;
    const isAtBottom = container.scrollHeight - container.scrollTop <= container.clientHeight + 10;

    if (!isAtBottom) {
      console.log("User scrolled up, do something...");
      // You can implement logic like showing a "Scroll to bottom" button
    }
  }
}
