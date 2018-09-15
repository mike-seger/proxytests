package com.net128.test.proxy.proxytest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "monitor.urlchecker")
@JsonIgnoreProperties("resolver")
public class UrlCheckerConfig {
    public static class Check {
        private String url;
        @JsonIgnore
        private String proxyUrl;
        @JsonIgnore
        private HttpMethod method;
        private int okStatus;
        @JsonIgnore
        private int timeoutSeconds;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getProxyUrl() {
            return proxyUrl;
        }

        public void setProxyUrl(String proxyUrl) {
            this.proxyUrl = proxyUrl;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public void setMethod(HttpMethod method) {
            this.method = method;
        }

        public int getOkStatus() {
            return okStatus;
        }

        public void setOkStatus(int okStatus) {
            this.okStatus = okStatus;
        }

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        @Override
        public String toString() {
            return "Check{" +
                    "url='" + url + '\'' +
                    ", proxyUrl='" + proxyUrl + '\'' +
                    ", method='" + method + '\'' +
                    ", okStatus=" + okStatus +
                    '}';
        }
    }

    private List<Check> checks;

    public List<Check> getChecks() {
        return checks;
    }

    public void setChecks(List<Check> checks) {
        this.checks = checks;
    }

    @Override
    public String toString() {
        return "UrlCheckerConfig{" +
                "checks=" + checks +
                '}';
    }
}
