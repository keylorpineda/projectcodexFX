package com.municipal.auth;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.SilentParameters;
import com.municipal.config.AppConfig;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AzureAuthService {

    private final PublicClientApplication application;
    private final Set<String> scopes;
    private IAuthenticationResult lastResult;

    public AzureAuthService() {
        try {
            String authority = AppConfig.require("azure.authority");
            this.application = PublicClientApplication.builder(AppConfig.require("azure.client-id"))
                    .authority(authority)
                    .build();
            this.scopes = resolveScopes();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Azure authentication", e);
        }
    }

    private Set<String> resolveScopes() {
        String scopesProperty = AppConfig.get("azure.scopes");
        if (scopesProperty == null || scopesProperty.isBlank()) {
            scopesProperty = AppConfig.get("azure.scope");
        }

        if (scopesProperty == null || scopesProperty.isBlank()) {
            throw new IllegalStateException("Azure scopes configuration is missing (azure.scopes or azure.scope)");
        }

        Set<String> parsedScopes = Arrays.stream(scopesProperty.split(","))
                .map(String::trim)
                .filter(scope -> !scope.isEmpty())
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        if (parsedScopes.isEmpty()) {
            throw new IllegalStateException("Azure scopes configuration must include at least one entry");
        }

        return Collections.unmodifiableSet(parsedScopes);
    }

    public CompletableFuture<IAuthenticationResult> signInInteractive() {
        try {
            InteractiveRequestParameters parameters = InteractiveRequestParameters
                    .builder(new URI(AppConfig.require("azure.redirect-uri")))
                    .scopes(scopes)
                    .prompt(Prompt.SELECT_ACCOUNT)
                    .build();
            return application.acquireToken(parameters)
                    .thenApply(result -> {
                        this.lastResult = result;
                        return result;
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public CompletableFuture<IAuthenticationResult> signInSilently() {
        if (lastResult == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("No cached account"));
        }
        SilentParameters parameters = SilentParameters.builder(scopes, lastResult.account()).build();
        try {
            return application.acquireTokenSilently(parameters);
        } catch (MalformedURLException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public Optional<IAuthenticationResult> getLastResult() {
        return Optional.ofNullable(lastResult);
    }
}
