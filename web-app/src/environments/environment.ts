export const environment = {
  serviceUrl: '',
  apiPath: '/api/v1/crypto/',
  liveTTL: 5000,

  providers: {
    githubAuth: {
      clientId: 'Ov23liP7aP7uz3BrzO8s',
      authorizationUrl: 'https://github.com/login/oauth/authorize',
      tokenUrl: 'https://github.com/login/oauth/access_token',
      userInfoUrl: 'https://api.github.com/user',
      scope: 'read:user user:email',
      redirectUri: 'http://localhost:4200/login/callback',
    }
  }
};
