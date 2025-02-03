import { Component } from '@angular/core';
import {User} from "../user/user";
import {NgForOf} from "@angular/common";

interface PortfolioItem {
  symbol: string;
  quantity: number;
}

@Component({
  selector: 'app-portfolio',
  imports: [
    NgForOf
  ],
  templateUrl: './portfolio.component.html',
  styleUrl: './portfolio.component.css'
})
export class PortfolioComponent {
  profit: number;
  portfolio: PortfolioItem[];

  constructor(private user: User) {
    user.portfolio.subscribe(portfolio => {
      this.profit = portfolio.getProfit();
      this.portfolio = Object.assign(new Array<PortfolioItem>(), portfolio.getStocks());
    })
  }

}
