# oEmbed Proxy 
Proxy for making oEmbed requests to known oEmbed providers

# Building and distribution

## Compile
    sbt compile

## Run tests
    sbt test

## Package and run locally
    ndla deploy local ndla/oembed-proxy

## Publish to nexus
    sbt publish

## Create Docker Image
    ./build.sh

## Deploy Docker Image
    ndla release ndla/oembed-proxy
    ndla deploy <environment> ndla/oembed-proxy
        

