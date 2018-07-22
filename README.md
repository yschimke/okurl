# OkHttp Social Client

[![CircleCI](https://circleci.com/gh/yschimke/okurl.svg?style=svg)](https://circleci.com/gh/yschimke/okurl)

A curl like client based on OkHttp with tight integration for Mac OSX

```
$ brew install yschimke/tap/okurl

$ okurl --authorize twitter

$ twitterapi '/1.1/statuses/show.json?id=733545978398040064'
...
``` 

If bash completion is installed at the same time, consider activating it with

```
Add the following line to your ~/.bash_profile:
  [ -f /usr/local/etc/bash_completion ] && . /usr/local/etc/bash_completion
```  

# Read the [Wiki](https://github.com/yschimke/okurl/wiki) for more information

## Requirements

- Java 8+
- Mac OSX (untested elsewhere)

## Features

- Login (token generation) support and automatic authentication for
    - Twitter
    - Facebook
    - Uber
- Javascript scripting support
- simple alias "twitterapi /1.1/statuses/user_timeline.json"
- OpenSC government smartcard support e.g. Estonian ID card
- OSX integration e.g. launch Preview for images and homebrew install
- HTTP/2 support and protocol selection

## Future Development

- Support for more API authentication schemes, hopefully in a purely configuration driven manner
- Switch between different tokens
