# Tracking bot
This is tracking bot for Wire. This bot, when is present in the conversation, will track all activity in that conversation
and store it as events in Events table.
Events table:
```
messageId UUID PRIMARY KEY,
conversationId UUID NOT NULL,
type VARCHAR NOT NULL,
payload VARCHAR NOT NULL,
time TIMESTAMP NOT NULL
```

## Exposed endpoints on 8080 port:
```
    POST    /bots                     # must be visible to Wire BE
    POST    /bots/{bot}/messages      # must be visible to Wire BE
    GET     /status                   # use this endpoint as liveness probe
    GET     /swagger                  # API documentation  
    GET     /version                  # prints the version
```

## Environment variables:
```  
SERVICE_TOKEN  # Tracking bot service token. Obtained from Wire   
WIRE_API_HOST  # Wire Backend DNS. Default: https://prod-nginz-https.wire.com
DB_DRIVER      # DBI driver. `org.postgresql.Driver` by default
DB_URL         # Database URL. for example: jdbc:postgresql://<HOST>:<PORT>/<DB_NAME>  
DB_USER        # Database user
DB_PASSWORD    # Database password 
HTTP_PORT      # Web server port. 8080 by default
ADMIN_PORT     # Web server admin port. 8081 by default  
LOG_LEVEL      # Log level: INFO by default. Other levels: ERROR, WARN, DEBUG 
```

## Build docker image from source code
docker build -t $DOCKER_USERNAME/tracking-bot:latest .

## Example of Docker run command
```
docker run \    
-e SERVICE_TOKEN='secret' \
-e DB_URL='jdbc:postgresql://docker.for.mac.localhost/tracker' \
-p 80:8080 \
--name tracker --rm $DOCKER_USERNAME/tracking-bot:latest
```

## Start the server locally:
java -Ddw.serviceToken='SjeGCoSqWRljnygV_stBh6_l' -jar target/tracking.jar server tracking.yaml
