/*
 * Copyright 2012-2023 The Feign Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package feign;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import feign.Request.HttpMethod;

@SuppressWarnings("deprecation")
class ResponseTest {

  @Test
  void reasonPhraseIsOptional() {
    Response response = Response.builder()
        .status(200)
        .headers(Collections.<String, Collection<String>>emptyMap())
        .request(Request.create(HttpMethod.GET, "/api", Collections.emptyMap(), null, Util.UTF_8))
        .body(new byte[0])
        .build();

    assertThat(response.reason()).isNull();
    assertThat(response.toString()).startsWith("HTTP/1.1 200");
  }

  @Test
  void canAccessHeadersCaseInsensitively() {
    Map<String, Collection<String>> headersMap = new LinkedHashMap<>();
    List<String> valueList = Collections.singletonList("application/json");
    headersMap.put("Content-Type", valueList);
    Response response = Response.builder()
        .status(200)
        .headers(headersMap)
        .request(Request.create(HttpMethod.GET, "/api", Collections.emptyMap(), null, Util.UTF_8))
        .body(new byte[0])
        .build();
    assertThat(response.headers())
        .hasEntrySatisfying("content-type", value -> {
          assertThat(value).contains("application/json");
        }).hasEntrySatisfying("Content-Type", value -> {
          assertThat(value).contains("application/json");
        });
  }

  @Test
  void headerValuesWithSameNameOnlyVaryingInCaseAreMerged() {
    Map<String, Collection<String>> headersMap = new LinkedHashMap<>();
    headersMap.put("Set-Cookie", Arrays.asList("Cookie-A=Value", "Cookie-B=Value"));
    headersMap.put("set-cookie", Collections.singletonList("Cookie-C=Value"));

    Response response = Response.builder()
        .status(200)
        .headers(headersMap)
        .request(Request.create(HttpMethod.GET, "/api", Collections.emptyMap(), null, Util.UTF_8))
        .body(new byte[0])
        .build();

    assertThat(response.headers()).hasEntrySatisfying("set-cookie", value -> {
      assertThat(value).contains("Cookie-A=Value", "Cookie-B=Value", "Cookie-C=Value");
    });
  }

  @Test
  void headersAreOptional() {
    Response response = Response.builder()
        .status(200)
        .request(Request.create(HttpMethod.GET, "/api", Collections.emptyMap(), null, Util.UTF_8))
        .body(new byte[0])
        .build();
    assertThat(response.headers()).isNotNull().isEmpty();
  }

  @Test
  void support1xxStatusCodes() {
    Response response = Response.builder()
        .status(103)
        .request(Request.create(HttpMethod.GET, "/api", Collections.emptyMap(), null, Util.UTF_8))
        .body((Response.Body) null)
        .build();

    assertThat(response.status()).isEqualTo(103);
  }

  @Test
  void statusCodesOfAnyValueAreAllowed() {
    Lists.list(600, 50, 35600).forEach(statusCode -> {
      Response response = Response.builder()
          .status(statusCode)
          .request(Request.create(HttpMethod.GET, "/api", Collections.emptyMap(), null, Util.UTF_8))
          .body((Response.Body) null)
          .build();

      assertThat(response.status()).isEqualTo(statusCode);
    });
  }
}
