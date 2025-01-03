export class CryptoQuote {
  name:   string;
  symbol: string;
  quoteDate: Date;
  quotePrice: Number;
}

export class CryptoType {
  name:   string;
  symbol: string;

  constructor(name: string, symbol: string) {
    this.name = name;
    this.symbol = symbol;
  }

  private static TYPES: Map<string, CryptoType> = new Map<string, CryptoType>();
  public static readonly DEFAULT_TYPE = new CryptoType("bitcoin", "btc");

  static setType(ct: CryptoType) {
    CryptoType.TYPES.set(ct.symbol.toLowerCase(), ct);
  }

  static getType(symbol: string) {
    return CryptoType.TYPES.get(symbol.toLowerCase())!;
  }
}
