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
```

## basicauth-reverseproxy
https://hub.docker.com/r/pottava/proxy/
https://github.com/pottava/basicauth-reverseproxy

```
curl -u "user1:pass1" -x http://localhost:17780/  http://www.google.com
```

