import {AfterViewInit, Component, Renderer2, ViewChild} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {NgIf} from "@angular/common";
import {environment} from "../../environments/environment";

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements AfterViewInit {
  currentUrl: string;
  @ViewChild('adminFrame') adminFrame: any;

  constructor(private renderer: Renderer2, private activatedRoute: ActivatedRoute) {}

  ngAfterViewInit() {
    this.activatedRoute.params.subscribe((params) => {
      // Access the current path after navigation
      this.currentUrl = params['type'];

      this.renderer.setProperty(this.adminFrame.nativeElement, "src", environment.serviceUrl + this.currentUrl);
    });
  }
}
