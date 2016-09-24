# OkHttp Social Client

[![Build Status](https://travis-ci.org/yschimke/oksocial.svg?branch=master)](https://travis-ci.org/yschimke/oksocial)

A curl like client based on OkHttp with tight integration for Mac OSX

```
$ brew install yschimke/tap/oksocial

$ oksocial --authorize twitter

$ twitterapi '/1.1/statuses/show.json?id=733545978398040064'
...
``` 

# Read the [Wiki](https://github.com/yschimke/oksocial/wiki) for more information

## Requirements

- Java 8+
- Mac OSX (untested elsewhere)

## Features

- Login (token generation) support and automatic authentication for
    - Twitter
    - Facebook
    - Uber
- Javascript scripting support
- simple alias "./twitterapi /1.1/statuses/user_timeline.json"
- OpenSC government smartcard support e.g. Estonian ID card
- OSX integration e.g. launch Preview for images and homebrew install
- HTTP/2 support and protocol selection

## Future Development

- Support for more API authentication schemes, hopefully in a purely configuration driven manner
- Switch between different tokens
