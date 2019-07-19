package com.soze.defense.client;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient {

  private static final Logger LOG = LoggerFactory.getLogger(HttpClient.class);

  private final OkHttpClient client = new OkHttpClient();

  private final String baseUrl;

  public HttpClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String get() {
    LOG.info("Making a GET request to {}", baseUrl);
    Request request = new Request.Builder().url(baseUrl).build();

    try (Response response = client.newCall(request).execute()) {
      return response.body().string();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }


}
