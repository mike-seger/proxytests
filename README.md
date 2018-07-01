# Test an authenticating proxy

```
docker-compose up -d
export http_proxy=http://user1:pass1@127.0.0.1:3128
export https_proxy=http://user1:pass1@127.0.0.1:3128
```

## squid
```
curl http://www.google.com
curl https://www.google.com
java -Djdk.http.auth.tunneling.disabledSchemes= \
	-Djdk.http.auth.proxying.disabledSchemes= \
	-Dhttpproxy="http://user1:pass1@localhost:3128" \
	-Dhttpsproxy="http://user1:pass1@localhost:3128" \
	TestProxy "-"  "http://www.google.com"
```

## basicauth-reverseproxy
https://hub.docker.com/r/pottava/proxy/
https://github.com/pottava/basicauth-reverseproxy

```
curl -u "user1:pass1" -x http://localhost:17780/  http://www.google.com
```

## References:
https://blog.typodrive.com/2016/08/18/simple-squid-on-docker-setup/  
https://github.com/sameersbn/docker-squid  
https://wiki.squid-cache.org/Features/Authentication  
https://medium.com/@salmaan.rashid/multi-mode-squid-proxy-container-running-ssl-bump-622128b8482a  
https://docs.oracle.com/javase/9/docs/api/java/net/doc-files/net-properties.html#Proxies  
https://stackoverflow.com/questions/41505219/unable-to-tunnel-through-proxy-proxy-returns-http-1-1-407-via-https  
