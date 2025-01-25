import {AfterViewInit, Component, Renderer2, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  standalone: true,
  styleUrl: './admin.component.css'
})

export class AdminComponent implements AfterViewInit {
  @ViewChild('adminFrame') adminFrame: any;

  constructor(private renderer: Renderer2, private router: Router, private activatedRoute: ActivatedRoute) {}

  ngAfterViewInit() {
    this.activatedRoute.params.subscribe((params) => {
      // Access the current path after navigation
      let currentUrl = params['type'];

      if (currentUrl == 'grafana') {
        currentUrl += '/d/spingbit/spring-bit-metrics';
      }
      if (!currentUrl.startsWith("/")) {
        currentUrl = "/" + currentUrl;
      }

      this.renderer.setProperty(this.adminFrame.nativeElement, "src", window.location.origin + currentUrl);
      //window.open(window.location.origin + currentUrl, "_blank");

      //this.router.navigate(['/']);
    });
  }
}
