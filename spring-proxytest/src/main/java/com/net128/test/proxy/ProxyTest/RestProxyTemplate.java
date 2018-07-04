package com.net128.test.proxy.ProxyTest;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;

@Component
public class RestProxyTemplate {

    private RestTemplate restTemplate;

    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;

    @Value("${proxy.http}")
    private String proxyHttp;

    @PostConstruct
    public void init() throws Exception {
        parseUri(proxyHttp);;
        this.restTemplate = new RestTemplate();

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), new UsernamePasswordCredentials(proxyUser, proxyPassword));

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.useSystemProperties();
        clientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
        clientBuilder.setDefaultCredentialsProvider(credsProvider);
        clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());

        // trust all SSL
//        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
//        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
//                .loadTrustMaterial(null, acceptingTrustStrategy)
//                .build();
//        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
//        clientBuilder.setSSLSocketFactory(csf);

        CloseableHttpClient client = clientBuilder.build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(client);

        restTemplate.setRequestFactory(factory);
    }

    private void parseUri(String uriString) throws URISyntaxException {
        URI uri=new URI(uriString);
        String authority = uri.getAuthority();
        if (authority != null) {
            int pos = authority.lastIndexOf("@");

            if (pos>0) {
                String userPass=authority.substring(0, pos);
                String [] userInfo = userPass.split(":");
                this.proxyUser = userInfo[0];
                if(userInfo.length>1) {
                    this.proxyPassword=userInfo[1];
                }
            }
        }

        proxyHost = uri.getHost();
        proxyPort = uri.getPort();
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}