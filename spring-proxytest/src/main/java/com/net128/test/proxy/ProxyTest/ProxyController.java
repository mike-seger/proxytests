package com.net128.test.proxy.ProxyTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProxyController {
    @Autowired
    private RestProxyTemplate restProxyTemplate;

    @ResponseBody
    @RequestMapping(value = "/proxy")
    public String proxy(@RequestParam String url, @RequestParam String data, HttpMethod method) throws Exception {
        HttpEntity<String> request = new HttpEntity<>(data);
        ResponseEntity<String> result = restProxyTemplate.getRestTemplate().exchange(url, method, request, String.class);
        return result.getBody();
    }
}
