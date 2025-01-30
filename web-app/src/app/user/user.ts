import {Injectable} from "@angular/core";
import {CryptoAction, UserDetails} from "../service/service.types";
import {AuthService} from "../service/auth.service";
import {BehaviorSubject} from "rxjs";
import {Portfolio} from "./user.types";


@Injectable({
  providedIn: 'root'  // Ensures this is a singleton across the app
})
export class User {
  private userDetails: UserDetails;

  private cryptoActions: CryptoAction[] = [];

  private userPortfolio: Portfolio = new Portfolio();

  private portfolioSource = new BehaviorSubject<Portfolio>(this.userPortfolio);
  public portfolio = this.portfolioSource.asObservable();

  constructor(authService: AuthService) {
    authService.userDetails.subscribe(userDetail => this.userDetails = userDetail);
    this.userPortfolio.addMoney(100);
    this.portfolioSource.next(this.userPortfolio);
  }

  addCryptoAction(cryptoAction: CryptoAction) {
    this.cryptoActions.push(cryptoAction);

    if (cryptoAction.operation == 'sell') {
      cryptoAction.quantity = this.userPortfolio.sellStock(
        cryptoAction.symbol, cryptoAction.quantity, cryptoAction.quotePrice.valueOf());
    } else if (cryptoAction.operation == 'buy') {
      cryptoAction.quantity = this.userPortfolio.buyStock(
        cryptoAction.symbol, cryptoAction.quantity, cryptoAction.quotePrice.valueOf());
    }

    this.portfolioSource.next(this.userPortfolio);
    return cryptoAction;
  }

}
