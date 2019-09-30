package com.signexample.sign.service;

import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.client.auth.OAuth;
import com.signexample.sign.config.DSConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DSBase {

  private static final long TOKEN_EXPIRATION_IN_SECONDS = 3600;
  private static final long TOKEN_REPLACEMENT_IN_MILLISECONDS = 10 * 60 * 1000;

  private static OAuth.Account _account;
  private static File privateKeyTempFile = null;
  private static long expiresIn;
  private static String _token = null;
  protected final ApiClient apiClient;

  public DSBase(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  protected static String getAccountId() {
    return _account.getAccountId();
  }

  protected void checkToken() throws IOException, ApiException {
    if (_token == null
      || (System.currentTimeMillis() + TOKEN_REPLACEMENT_IN_MILLISECONDS) > expiresIn) {
      updateToken();
    }
  }

  private void updateToken() throws IOException, ApiException {
    System.out.println("\nFetching an access token via JWT grant...");

    java.util.List<String> scopes = new ArrayList<>();
    // Only signature scope is needed. Impersonation scope is implied.
    scopes.add(OAuth.Scope_SIGNATURE);
    String privateKey = DSConfig.PRIVATE_KEY.replace("\\n", "\n");
    byte[] privateKeyBytes = privateKey.getBytes();
    apiClient.setOAuthBasePath(DSConfig.DS_AUTH_SERVER);

    OAuth.OAuthToken oAuthToken = apiClient.requestJWTUserToken(
      DSConfig.CLIENT_ID,
      DSConfig.IMPERSONATED_USER_GUID,
      scopes,
      privateKeyBytes,
      TOKEN_EXPIRATION_IN_SECONDS);
    apiClient.setAccessToken(oAuthToken.getAccessToken(), oAuthToken.getExpiresIn());
    System.out.println("Done. Continuing...\n");

    if (_account == null)
      _account = this.getAccountInfo(apiClient);
    // default or configured account id.
    apiClient.setBasePath(_account.getBaseUri() + "/restapi");

    _token = apiClient.getAccessToken();
    expiresIn = System.currentTimeMillis() + (oAuthToken.getExpiresIn() * 1000);
  }

  private OAuth.Account getAccountInfo(ApiClient client) throws ApiException {
    OAuth.UserInfo userInfo = client.getUserInfo(client.getAccessToken());

    if (DSConfig.TARGET_ACCOUNT_ID == null || DSConfig.TARGET_ACCOUNT_ID.length() == 0) {
      List<OAuth.Account> accounts = userInfo.getAccounts();

      OAuth.Account acct = this.find(accounts, member -> (member.getIsDefault().equals("true")));

      if (acct != null) return acct;

      acct = this.find(accounts, member -> (member.getAccountId().equals(DSConfig.TARGET_ACCOUNT_ID)));

      if (acct != null) return acct;

    }

    return null;
  }

  private OAuth.Account find(List<OAuth.Account> accounts, ICondition<OAuth.Account> criteria) {
    for (OAuth.Account acct : accounts) {
      if (criteria.test(acct)) {
        return acct;
      }
    }
    return null;
  }

  interface ICondition<T> {
    boolean test(T member);
  }
}
