# Mashup service

A spring boot based microservice which exposes an endpoint to fetch artist details using following endpoint
``` java
http://<server ip>:6200/api/v1/artist-search/artist/{mbid}
```
`{mbid}` is an artist's identifier which should be as per [MusicBrainz identifier](https://musicbrainz
.org/doc/MusicBrainz_Identifier) format.

`Basic auth has been enabled on the API.`
 `Credentials required are`
    `Username: user`
    `Password: password`

If an artist is found then a JSON with following format will be returned
```
{
  "artistId": "string", // artist id (Mbid) from the request
  "description": "string", // Details about the artist, fetched from Discogs API
  "albums": [ // Albums of the artist
    {
      "albumId": "string", // Album's mbid fetched from MusicBrainz
      "title": "string", // Title of the album
      "images": [ // A list of http links to all the cover art images of the album, fetched from CoverArtArchive API
        "string"
      ]
    },
    ...
  ],
  "links": [
    {
        "rel": "string", // Relation of the link
        "href": "string" // Relative URL of the resource
    }
  ]
}
```

It can return following error codes

* 400 - Bad Request
* 401 - Unauthorized
* 404 - Not found
* 429 - Too Many request
* 500 - Internal Server Error


Details of error code is explained later under design section.

## Build and Run

### Running Locally
To Build on local run following command
```shell
./mvnw spring-boot:run
```
It will download the maven if not present on the system. It will also start the application, by default on port 6200.
API can be accessed using http://localhost:6200/api/v1/artist-search/artist/f27ec8db-af05-4f36-916e-3d57f91ecf5e

### On Docker
Project is dockerized and can be run using Docker.
Docker build do a maven build and then generates a docker image which can be used to run the service.

```shell
docker build . -t <Name of the image>:<tag of image>
```

And here is the command to run a container of the REST service (assume docker daemon is available):
```shell
docker run -p 6200:6200 <Name of the image>:<tag of image>
```

### Project Tech
Microservice is built using following tech stack

* Spring Boot 2.2.0.M1 - Spring boot is one of the most widely used framework to develop microservices. It provides support for multiple libraries with good support. It also have a preference of configuration over coding which makes development fast.
* Netty- Netty is really useful to run reactive applications. It's reactive and asynchoronus model makes it an ideal candiate to use in cases where parallelism is must.
* Reactor- Spring boot supports reactor with the Webflux related stack. With this case where we need to call several API's togther, it makes a perfect fit. As we have to call several API's in order to retrieve albums therefore with reactive streams we can do it both in asynchornous way and by creating backpressure too.
* JDK 1.10
* Maven 3.5.2
* Swagger for REST API documentation
* Spring HateOAS- for HateOas support.
* Spring security- A very basic spring security has been used in order to make API calls safe. An extended solution can be made in order to support more better handling of user grants and passwords.
* Guava- It has been used for providing both caching and throttling.
* Docker- To run application in containers.



## Project and API Design
Some of the design constraints are

### Logic

In order to fetch the response for the artist following API's were used.

* MusicBrainz Artist API-  It was used to fetch artist details. It also gave album MBID's of the artist as part of release groups. It also contains relations for Discogs which was used to fetch artist description.
```
http://musicbrainz.org/ws/2/artist/{mbid}?&fmt=json&inc=url-rels+release-groups
```

* Discogs API- It was used to fetch artist profile based on discog id.
```
https://api.discogs.com/artists/{discog id}
```

* Cover Art Archive API- It is used to fetch album images. Album mbid was used in order to fetch it.
```
http://coverartarchive.org/release-group/{album mbid}
```

### Response time
In order to fetch artist details followinng REST calls are made

* One call to MusicBrainz artist API to get list of albums and relations
* One call to Discogs API to get the profile description information
* For each album, make one call to CoverArtArchive API to fetch its image links

So for one request we need to hit several request. It can slow down the response time significatnly if calls are made in sequence but with the use of reactive stack all these calls are made parallely and then back pressure is created in order to build the response.

Along with this all the entities fetched from the API's are cached using Guava In memory Cache. This improves performance a lot for the calls which were already made. Since this is an in memory solution, so we can get into OOM pretty soon. So another caching solution like Redis can be used for a long term solution.

Cache can be tuned with following parameters
```
mashup:
  artist-search-service:
    cache:
          retention-days: 1 // Cache will be evicted after 1 day
```


### Throttling
Request to the service are throttled in order to achieve high availbility of the service. Downstream calls to the API's like MusicBrainz and Discogs are rate limited so it makes sense to throttle the incoming request.

Right now Guava's RateLimiter is used to provide it but in a more longer term Netflix's Zuul along with rate limiting library can be used as it provides option to store the data on database rather than keeping it in memory.

Throttling can be controlled with following parameters
```
viaplay:
  artist-search-service:
    throttling:
          requestPerSecond: 10 // How much request per second are allowed
          timeout: 10 // Time out in millisecond to acuire permit else rate limit excpetion is thrown

```


### Error Handling
Followinng errors will be emitted by the application
* 400 - Bad Request- If invalid mbid is sent
* 401 - Unauthorized- If invalid credentials are passed while calling API
* 404 - Artist Not Found- if artist is not found on MusicBrainz
* 429 - Too many calls- If rate limit is reached
* 500 - Internal Server Error- If some error occurs or MusicBrainz API become unreachable

In case when errors are recieved from Discogs and CoverArtArchive then they are not propgated back but are logged.

### Resilience
If third party API's times out then fallbacks are used through Spring Webflux support but it still makes API vulnerable.
In order to have more robust solution Netflix Hystrix can be used on top pf Spring Webclient as it is perfect to handle
circuit breaker.

Timeouts have been configured using following properties
```
mashup:
  artist-search-service:
    musicbranz-service:
      timeoutSeconds: 2 // Timeout for the MusicBrainz API
    coverartarchive-service:
      timeoutSeconds: 2 // Timeout for the CoverArtArchive API
    discogs-service:
      timeoutSeconds: 2 // Timeout for the discogs API
```

### Security
API is protected behind Spring security. Right now a basic auth has been provided with the help of Spring security  but a more advance solution like OAuth2.0 can be provided with the help of Redis to manage the tokens inn order to protect the API against outside risk.

Password for basic auth can be configured using
```
spring:
  security:
    user:
      password: password // Password for the Basic Auth of the API
```

### Metrics
Prometheus support has been added through Spring actuator. Prometheus can scrap the data emitted by application and performance of application can be tracked using Prometheus UI.
It can be accessed using http://localhost:6200/actuator/prometheus

### Why Discogs was choosen
After evaluating several sources, I choose Discogs because
* Content provided by discogs was more relevant. Profile of the artist is a good summary as compared to other sources.
* API's are well documented and clear. Discog ID can easily be fetched from MusicBrainz API response. Same was not possible other sources.
* REST API's were good in terms of performance and coherent as compared to other API's


## Documentation
Documentation of the API can be accessed via Swagger UI on the URL: `http://<host>:6200/swagger-ui.html`

## Improvements
* More test cases can be added. Due to lack of time only few integration and unit test case are added but more can be added for higher quality.
* Netflix hystrix can be used to be make calls.
* Redis can be used for caching.
* Advance level of Spring Security.
* Netflix Zuul for throttling and act as gateway.
* Only Self ref are being used with HATEOAS on Artist object, images under Album can also be converted with HATEOAS design

## Other MBID's
```
f27ec8db-af05-4f36-916e-3d57f91ecf5e
35f866dc-c061-48ba-8157-cf2e0eac4857
45a663b5-b1cb-4a91-bff6-2bef7bbfdd76
05cbaf37-6dc2-4f71-a0ce-d633447d90c3
7944ed53-2a58-4035-9b93-140a71e41c34
f59c5520-5f46-4d2c-b2c4-822eabf53419
```