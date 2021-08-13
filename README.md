# Sonos NowPlaying

Displays your current playing sonos content (artist, title, cover).

https://www.nico-zimmermann.com/sonos/

To reset the displayed sonos group, add '?config' to the URL:

e.g. https://www.nico-zimmermann.com/sonos?config

## build

### config
`cp src/main/resources/config.properties.template src/main/resources/config.properties`

in config.properties set your sonos_client_id and sonos_client_secret from https://integration.sonos.com/integrations

### build

`mvn package`

copy target/sonos.war to your tomcat webapp directory

