/*
 * Copyright 2018 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.daex.sdk.core.service.security;

import io.daex.sdk.core.http.*;
import io.daex.sdk.core.util.ResponseConverterUtils;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;

import java.io.IOException;
import java.time.Instant;

/**
 * Retrieves, stores, and refreshes IAM tokens.
 */
public class IamTokenManager {
  private String clientId;
  private String clientSecret;
  private String url;
  private IamToken tokenData;

  private static final String DEFAULT_IAM_URL = "https://iam.daex.io/v1/oauth/token";
  private static final String GRANT_TYPE = "grant_type";
  private static final String REQUEST_CLIENT_ID = "client_id";
  private static final String REQUEST_GRANT_TYPE = "client_credentials";
  private static final String REQUEST_CLIENT_SECRET = "client_secret";

  public IamTokenManager(IamOptions options) {
    this.clientId = options.getClientId();
    this.clientSecret = options.getClientSecret();
    this.url = (options.getUrl() != null) ? options.getUrl() : DEFAULT_IAM_URL;
  }

  /**
   *
   * @return the valid access token
   */
  public String getToken() {
    String token;

    if (isAccessTokenExpired()) {
      // request new token
      token = requestToken();
    } else {
      // use valid managed token
      token = tokenData.getAccessToken();
    }

    return token;
  }

  /**
   * Request an IAM token using an API key. Also updates internal managed IAM token information.
   *
   * @return the new access token
   */
  private String requestToken() {
    RequestBuilder builder = RequestBuilder.post(RequestBuilder.constructHttpUrl(url, new String[0]));

    builder.header(HttpHeaders.CONTENT_TYPE, HttpMediaType.APPLICATION_FORM_URLENCODED);

    FormBody formBody = new FormBody.Builder()
        .add(GRANT_TYPE, REQUEST_GRANT_TYPE)
        .add(REQUEST_CLIENT_ID, clientId)
        .add(REQUEST_CLIENT_SECRET, clientSecret)
        .build();
    builder.body(formBody);

    tokenData = callIamApi(builder.build());
    tokenData.setExpiration(Instant.now().getEpochSecond()+tokenData.getExpiresIn());
    return tokenData.getAccessToken();
  }

  /**
   * Check if currently stored access token is expired.
   *
   * @return whether the current managed access token is expired or not
   */
  private boolean isAccessTokenExpired() {
    if (tokenData == null || tokenData.getExpiresIn() == null || tokenData.getExpiration() == null) {
      return true;
    }
    Long expirationTime = tokenData.getExpiration();
    Double currentTime = Math.floor(System.currentTimeMillis() / 1000);

    return expirationTime < currentTime;
  }


  /**
   * Executes call to IAM API and returns IamToken object representing the response.
   *
   * @param request the request for the IAM API
   * @return object containing requested IAM token information
   */
  private IamToken callIamApi(Request request) {
    Call call = HttpClientSingleton.getInstance().createHttpClient().newCall(request);
    ResponseConverter<IamToken> converter = ResponseConverterUtils.getObject(IamToken.class);

    try {
      okhttp3.Response response = call.execute();
      return converter.convert(response);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
