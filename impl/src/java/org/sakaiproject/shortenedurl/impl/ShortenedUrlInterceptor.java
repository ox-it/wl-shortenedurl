package org.sakaiproject.shortenedurl.impl;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.AbstractContentCopyUrlInterceptor;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UrlInterceptor in charge of changing shortened url back into the original url
 *
 * @author Colin Hebert
 */
public class ShortenedUrlInterceptor extends AbstractContentCopyUrlInterceptor {
    private static Pattern PATH_PATTERN = Pattern.compile("/+x/+(.+)");
    private ShortenedUrlService shortenedUrlService;
    private ServerConfigurationService scs;

    public boolean isUrlHandled(String url) {
        URI uri = URI.create(url);
        return (isLocalUri(uri) && PATH_PATTERN.matcher(uri.getPath()).matches());
    }

    private boolean isLocalUri(URI uri) {
        return ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) &&
                (uri.getHost() == null || getServerNames().contains(uri.getHost()));
    }

    public String convertUrl(String originalUrl) {
        URI uri = URI.create(originalUrl);
        Matcher pathMatcher = PATH_PATTERN.matcher(uri.getPath());
        if (pathMatcher.matches()) {
            return shortenedUrlService.resolve(pathMatcher.group(1));
        } else {
            throw new RuntimeException("Couldn't find the ShortenedUrl key in '" + originalUrl + "'");
        }
    }

    public String convertProcessedUrl(String processedUrl) {
        return shortenedUrlService.shorten(processedUrl);
    }

    public void setShortenedUrlService(ShortenedUrlService shortenedUrlService) {
        this.shortenedUrlService = shortenedUrlService;
    }

    public void setScs(ServerConfigurationService scs) {
        this.scs = scs;
    }

    private Collection<String> getServerNames() {
        Collection<String> serverNames = new ArrayList<String>(scs.getServerNameAliases());
        serverNames.add(scs.getServerName());
        return serverNames;
    }
}
