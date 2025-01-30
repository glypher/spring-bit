export class Stock {
  public symbol: string;
  public quantity: number;
}

export class Portfolio {
  private profit: number = 0;

  private stocks: Map<string, Stock> = new Map<string, Stock>();

  getProfit() {
    return this.profit;
  }

  getStocks() {
    let data = []
    for (let stock of this.stocks.values()) {
      data.push({symbol: stock.symbol, quantity: stock.quantity});
    }
    return data;
  }

  sellStock(symbol: string, quantity: number, price: number) {
    let stock = this.stocks.get(symbol);
    if (stock) {
      let count = Math.min(quantity, stock.quantity);
      stock.quantity -= count;
      let usd = this.stocks.get('usd')!;
      usd.quantity += price * count;

      return count;
    }

    return 0;
  }

  buyStock(symbol: string, quantity: number, price: number) {
    let stock = this.stocks.get(symbol) || Object.assign(new Stock(), {symbol: symbol, quantity: 0});
    this.stocks.set(symbol, stock);
    let usd = this.stocks.get('usd')!;

    let maxCount = usd.quantity / price;
    let count = Math.max(maxCount, stock.quantity);
    stock.quantity += count;
    usd.quantity -= price * count;

    return count;
  }

  addMoney(quantity: number) {
    let usd = this.stocks.get('usd') || Object.assign(new Stock(), {symbol: 'usd', quantity: 0});
    this.stocks.set('usd', usd);
    usd.quantity += quantity;
  }
}
