package com.net128.test.proxy.proxytest;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

public class RestProxyTemplate {

    private RestTemplate restTemplate;

    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;

    public RestProxyTemplate() {
        restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(factory);
    }
    public RestProxyTemplate(String proxyUrl) {
        this();
        init(proxyUrl);
    }

    public void init(String proxyUrl) {
        if(proxyUrl == null) {
            return;
        }
        parseUri(proxyUrl);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
            new AuthScope(proxyHost, proxyPort),
            new UsernamePasswordCredentials(proxyUser, proxyPassword));
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

        HttpComponentsClientHttpRequestFactory rf =
                (HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory();
        rf.setHttpClient(client);
        restTemplate.setRequestFactory(rf);
    }

//    private void registerKeyStore(String keyStoreName) {
//        try {
//            ClassLoader classLoader = this.getClass().getClassLoader();
//            InputStream keyStoreInputStream = classLoader.getResourceAsStream(keyStoreName);
//            if (keyStoreInputStream == null) {
//                throw new FileNotFoundException("Could not find file named '" + keyStoreName + "' in the CLASSPATH");
//            }
//
//            //load the keystore
//            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
//            keystore.load(keyStoreInputStream, null);
//
//            //add to known keystore
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            trustManagerFactory.init(keystore);
//
//            //default SSL connections are initialized with the keystore above
//            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
//            SSLContext sc = SSLContext.getInstance("SSL");
//            sc.init(null, trustManagers, null);
//            SSLContext.setDefault(sc);
//        } catch (IOException | GeneralSecurityException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private void parseUri(String uriString) {
        URI uri= null;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
           throw new RuntimeException("Unable to parse: "+uriString, e);
        }
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