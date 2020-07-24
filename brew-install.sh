#!/bin/sh

./build-native.sh
tar cf ./build/okurl.tar build/graal/okurl zsh/_okurl
brew reinstall ./okurl.rb
