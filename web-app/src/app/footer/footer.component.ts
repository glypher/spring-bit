// Generate a new component using Angular CLI:
// ng generate component server-footer

// server-footer.component.ts
import { Component } from '@angular/core';
import {NgForOf} from "@angular/common";
import {Router} from "@angular/router";

@Component({
  selector: 'app-footer',
  imports: [
    NgForOf,
  ],
  templateUrl: './footer.component.html',
  standalone: true,
  styleUrls: ['./footer.component.css']
})
export class FooterComponent {

  constructor(private router: Router) {
  }
  links = [
    {
      name: 'Grafana',
      url: '/grafana',
      icon: 'assets/logos/icons8-grafana-96.png'
    },
    {
      name: 'Zipkin',
      url: '/tracing',
      icon: 'assets/logos/zipkin-logo.png'
    },
    {
      name: 'Prometheus',
      url: '/prometheus',
      icon: 'assets/logos/icons8-prometheus-96.png'
    },
    {
      name: 'Eureka',
      url: '/discovery',
      icon: 'assets/logos/eureka-logo-150.png'
    }
  ];

  onClick(url: string) {
    this.router.navigate(['/admin' + url], { skipLocationChange: true });
  }
}
