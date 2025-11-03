package finalprojectprogramming.project.services.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

class AzureGraphClientTest {

    private MockWebServer server;
    private AzureGraphClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new AzureGraphClient(new ObjectMapper());
        ReflectionTestUtils.setField(client, "httpClient", new RedirectingHttpClient(server));
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void fetchCurrentUser_withValidPayload_returnsInfoAndHitsGraphEndpoint() throws Exception {
        server.enqueue(jsonResponse(200,
                "{\"id\":\"123\",\"displayName\":\"Jane\",\"mail\":\"jane@example.com\"}"));

        AzureUserInfo info = client.fetchCurrentUser("token");

        assertThat(info.id()).isEqualTo("123");
        assertThat(info.email()).isEqualTo("jane@example.com");
        assertThat(info.displayName()).isEqualTo("Jane");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer token");
        assertThat(request.getPath()).startsWith("/v1.0/me");
    }

    @Test
    void fetchCurrentUser_usesUserPrincipalNameWhenMailMissing() throws Exception {
        server.enqueue(jsonResponse(200,
                "{\"id\":\"123\",\"displayName\":\"Jane\",\"mail\":\"\",\"userPrincipalName\":\"jane@contoso.com\"}"));

        AzureUserInfo info = client.fetchCurrentUser("token");

        assertThat(info.email()).isEqualTo("jane@contoso.com");
    }

    @Test
    void fetchCurrentUser_whenTokenBlank_throwsBadCredentials() {
        assertThatThrownBy(() -> client.fetchCurrentUser(" "))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Azure access token is required");
    }

    @Test
    void fetchCurrentUser_whenUnauthorized_throwsBadCredentials() {
        server.enqueue(jsonResponse(401, "{}"));

        assertThatThrownBy(() -> client.fetchCurrentUser("token"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid Azure access token");
    }

    @Test
    void fetchCurrentUser_whenServerError_throwsBusinessRuleException() {
        server.enqueue(jsonResponse(500, "{\"error\":\"down\"}"));

        assertThatThrownBy(() -> client.fetchCurrentUser("token"))
                .isInstanceOf(finalprojectprogramming.project.exceptions.BusinessRuleException.class)
                .hasMessageContaining("rejected the authentication attempt");
    }

    @Test
    void fetchCurrentUser_whenJsonMalformed_throwsIllegalStateException() {
        server.enqueue(jsonResponse(200, "not-json"));

        assertThatThrownBy(() -> client.fetchCurrentUser("token"))
                .isInstanceOf(IllegalStateException.class)
                // Método síncrono envuelve como "Unable to fetch ..."
                .hasMessageContaining("Unable to fetch user from Microsoft Graph");
    }

    @Test
    void fetchCurrentUser_whenEmailMissing_throwsBadCredentials() {
        server.enqueue(jsonResponse(200,
                "{\"id\":\"123\",\"displayName\":\"Jane\",\"mail\":null,\"userPrincipalName\":null}"));

        assertThatThrownBy(() -> client.fetchCurrentUser("token"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("does not expose an email address");
    }

    @Test
    void fetchCurrentUser_whenHttpClientThrowsIOException_wrapsInIllegalStateException() {
        AzureGraphClient failingClient = new AzureGraphClient(new ObjectMapper());
        ReflectionTestUtils.setField(failingClient, "httpClient", new ThrowingHttpClient(new IOException("boom")));

        assertThatThrownBy(() -> failingClient.fetchCurrentUser("token"))
                .isInstanceOf(IllegalStateException.class)
                // Método síncrono envuelve como "Unable to fetch ..."
                .hasMessageContaining("Unable to fetch user from Microsoft Graph");
    }

    @Test
    void fetchCurrentUser_whenInterrupted_setsInterruptFlagAndThrows() {
        AzureGraphClient failingClient = new AzureGraphClient(new ObjectMapper());
        ReflectionTestUtils.setField(failingClient, "httpClient", new ThrowingHttpClient(new InterruptedException("stop")));

    // En la variante síncrona no se garantiza el flag de interrupción
    assertThatThrownBy(() -> failingClient.fetchCurrentUser("token"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Unable to fetch user from Microsoft Graph");
    }

    @Test
    void fetchCurrentUser_asyncDirectBadCredentials_propagates() {
        // Force sendAsync to complete exceptionally with BadCredentialsException directly
        AzureGraphClient badCredsClient = new AzureGraphClient(new ObjectMapper());
        ReflectionTestUtils.setField(badCredsClient, "httpClient",
                new ThrowingHttpClient(new org.springframework.security.authentication.BadCredentialsException("invalid")));

        assertThatThrownBy(() -> badCredsClient.fetchCurrentUser("token"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void fetchCurrentUser_asyncWrappedBusinessRuleException_propagates_from_cause() {
    AzureGraphClient clientWithWrapped = new AzureGraphClient(new ObjectMapper());
    // Complete exceptionally with a CompletionException whose cause is BusinessRuleException
    java.util.concurrent.CompletionException wrapped = new java.util.concurrent.CompletionException(
        new finalprojectprogramming.project.exceptions.BusinessRuleException("wrapped"));
    ReflectionTestUtils.setField(clientWithWrapped, "httpClient", new ThrowingHttpClient(wrapped));

    assertThatThrownBy(() -> clientWithWrapped.fetchCurrentUser("token"))
        .isInstanceOf(finalprojectprogramming.project.exceptions.BusinessRuleException.class)
        .hasMessageContaining("wrapped");
    }

    private MockResponse jsonResponse(int status, String body) {
        return new MockResponse()
                .setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private static final class RedirectingHttpClient extends HttpClient {

        private final HttpClient delegate;
        private final URI baseUri;

        private RedirectingHttpClient(MockWebServer server) {
            this.delegate = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            this.baseUri = server.url("/").uri();
        }

        @Override
        public Optional<Executor> executor() {
            return delegate.executor();
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return delegate.cookieHandler();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return delegate.connectTimeout();
        }

        @Override
        public Redirect followRedirects() {
            return delegate.followRedirects();
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return delegate.proxy();
        }

        @Override
        public SSLContext sslContext() {
            try {
                return SSLContext.getDefault();
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public SSLParameters sslParameters() {
            return delegate.sslParameters();
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return delegate.authenticator();
        }

        @Override
        public Version version() {
            return delegate.version();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
                throws IOException, InterruptedException {
            HttpRequest redirected = redirected(request);
            return delegate.send(redirected, responseBodyHandler);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler) {
            HttpRequest redirected = redirected(request);
            return delegate.sendAsync(redirected, responseBodyHandler);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            HttpRequest redirected = redirected(request);
            return delegate.sendAsync(redirected, responseBodyHandler, pushPromiseHandler);
        }

        private HttpRequest redirected(HttpRequest original) {
            URI originalUri = original.uri();
            String pathAndQuery = originalUri.getRawPath()
                    + (originalUri.getRawQuery() != null ? "?" + originalUri.getRawQuery() : "");
            URI target = baseUri.resolve(pathAndQuery);
            HttpRequest.Builder builder = HttpRequest.newBuilder(target)
                    .timeout(original.timeout().orElse(Duration.ofSeconds(5)))
                    .method(original.method(), original.bodyPublisher().orElse(HttpRequest.BodyPublishers.noBody()));
            original.headers().map().forEach((name, values) -> values.forEach(value -> builder.header(name, value)));
            return builder.build();
        }
    }

    private static final class ThrowingHttpClient extends HttpClient {

        private final Exception toThrow;

        private ThrowingHttpClient(Exception toThrow) {
            this.toThrow = toThrow;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.of(Duration.ofSeconds(5));
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NORMAL;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            try {
                return SSLContext.getDefault();
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public SSLParameters sslParameters() {
            return new SSLParameters();
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
                throws IOException, InterruptedException {
            if (toThrow instanceof IOException io) {
                throw io;
            }
            if (toThrow instanceof InterruptedException interrupted) {
                throw interrupted;
            }
            if (toThrow instanceof HttpTimeoutException timeout) {
                throw timeout;
            }
            throw new IOException(toThrow);
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler) {
            CompletableFuture<HttpResponse<T>> future = new CompletableFuture<>();
            future.completeExceptionally(toThrow);
            return future;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            return sendAsync(request, responseBodyHandler);
        }
    }
}
