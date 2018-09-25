package com.net128.test.proxy.proxytest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class UrlCheckerHealthIndicator implements HealthIndicator {

    @Autowired
    private UrlCheckerConfig urlCheckerConfig;

    private List<CheckedUrl> checkedUrls =new ArrayList<>();

    @PostConstruct
    public void init() {
        if(urlCheckerConfig.getChecks()!=null) {
            for(UrlCheckerConfig.Check check : urlCheckerConfig.getChecks()) {
                if(check.disabled) {
                    continue;
                }
                CheckedUrl checkedUrl=new CheckedUrl();
                checkedUrl.restTemplate=new RestProxyTemplate(
                    check.getProxyUrl(), check.trustAllSsl);
                checkedUrl.check=check;
                checkedUrl.url=check.getUrl();
                HttpComponentsClientHttpRequestFactory rf =
                    (HttpComponentsClientHttpRequestFactory) checkedUrl.restTemplate.getRequestFactory();
                rf.setConnectionRequestTimeout(check.getTimeoutSeconds() * 1000);
                rf.setReadTimeout(check.getTimeoutSeconds() * 1000);
                rf.setConnectTimeout(check.getTimeoutSeconds() * 1000);
                checkedUrls.add(checkedUrl);
            }
        }
    }

    public boolean checkAll() {
        int successCount=checkedUrls.stream().parallel().mapToInt(checkedUrl -> {
            checkedUrl.httpStatus = -1;
            checkedUrl.error=null;
            try {
                ResponseEntity<String> result = checkedUrl.restTemplate.exchange(
                    checkedUrl.check.getUrl(), checkedUrl.check.getMethod(), null, String.class);
                checkedUrl.httpStatus = result.getStatusCodeValue();
                int ok = checkedUrl.httpStatus == checkedUrl.check.getOkStatus() ? 1 : 0;
                if(ok == 0) {
                    checkedUrl.error="Got HTTP status:"+checkedUrl.httpStatus +
                        ", but expected:"+checkedUrl.check.getOkStatus();
                    checkedUrl.status=Status.DOWN.toString();
                }
                return ok;
            } catch(Exception e) {
                checkedUrl.error=e.getMessage();
                checkedUrl.status=Status.DOWN.toString();
                e.printStackTrace();
                return 0;
            }
        }).sum();
        return checkedUrls.size()==successCount;
    }

    @Override
    public Health health() {
        Health.Builder hb=Health.up();
        if (!checkAll()) {
            hb = Health.down();
        }
        return hb.withDetail("checks", checkedUrls).build();
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private static class CheckedUrl {
        private UrlCheckerConfig.Check check;
        private RestTemplate restTemplate;
        private int httpStatus;
        public String status = Status.UP.toString();
        public String url;
        public String error;
    }

    @Configuration
    @ConfigurationProperties(prefix = "monitor.urlchecker")
    @JsonIgnoreProperties("resolver")
    public static class UrlCheckerConfig {
        public static class Check {
            private String url;
            @JsonIgnore
            private String proxyUrl;
            @JsonIgnore
            private HttpMethod method;
            private int okStatus;
            @JsonIgnore
            private int timeoutSeconds;
            @JsonIgnore
            private boolean trustAllSsl;

            private boolean disabled;

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

            public boolean isTrustAllSsl() {
                return trustAllSsl;
            }

            public void setTrustAllSsl(boolean trustAllSsl) {
                this.trustAllSsl = trustAllSsl;
            }

            public boolean isDisabled() {
                return disabled;
            }

            public void setDisabled(boolean disabled) {
                this.disabled = disabled;
            }

            @Override
            public String toString() {
                return "Check{" +
                        "url='" + url + '\'' +
                        ", proxyUrl='" + proxyUrl + '\'' +
                        ", method=" + method +
                        ", okStatus=" + okStatus +
                        ", timeoutSeconds=" + timeoutSeconds +
                        ", trustAllSsl=" + trustAllSsl +
                        ", disabled=" + disabled +
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

}