# Spring Boot 2 Proxy RestTemplate example

## Run the example server application
```
$ mvn clean package
$ java -jar target/myapplication-0.0.1-SNAPSHOT.jar
# or
$ mvn spring-boot:run
```

## Test using curl
```
unset http_proxy
unset https_proxy

# call health check (incl. url checker)
curl -s localhost:8080/actuator/health | jq .

# GET
curl -X GET -v "http://localhost:8080/proxy?url=https://httpbin.org/anything&data=1234"
# POST
curl -d "url=https://httpbin.org/anything&data=1234" -v http://localhost:8080/proxy
```
