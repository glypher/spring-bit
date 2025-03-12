export class CryptoQuote {
  name:   string;
  symbol: string;
  quoteDate: Date;
  quotePrice: Number;
}

export class CryptoAction {
  name:   string;
  symbol: string;
  operation: string;
  quantity: number;
  quoteDate: Date;
  quotePrice: Number;
}

export class CryptoInfo {
  correlation: { [key: string]: { [key: string]: number } };
}

export class Chat {
  reply: string;
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

export class CryptoTypeImg extends CryptoType {
  src:    string;
}

export enum AuthType {
  Github,
  Facebook,
  Google,
  Keycloak
}

export class UserDetails {
  username: string;
  avatarUrl: string;

  constructor(username: string, avatarUrl: string) {
    this.username = username;
    this.avatarUrl = avatarUrl;
  }

  public static readonly DEFAULT_USER = new UserDetails("", "");
}
