#!/bin/sh -e

./gradlew nativeImage
tar cf ./build/okurl.tar build/graal/okurl zsh/_okurl
brew reinstall ./okurl.rb
