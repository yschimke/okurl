#!/bin/sh -e

./gradlew distTar
brew reinstall ./okurl.rb
