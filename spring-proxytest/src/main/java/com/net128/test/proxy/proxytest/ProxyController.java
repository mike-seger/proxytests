package com.net128.test.proxy.proxytest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Controller
public class ProxyController {
    private RestTemplate restTemplate;

    @Value("${proxy.http}")
    private String proxyHttp;

    @PostConstruct
    public void init() {
        restTemplate = new RestProxyTemplate(proxyHttp).getRestTemplate();
    }

    @ResponseBody
    @RequestMapping(value = "/proxy")
    public String proxy(@RequestParam String url, @RequestParam String data, HttpMethod method) throws Exception {
        HttpEntity<String> request = new HttpEntity<>(data);
        ResponseEntity<String> result = restTemplate.exchange(url, method, request, String.class);
        return result.getBody();
    }
}
