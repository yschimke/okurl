#!/bin/sh

./gradlew assemble
mkdir -p build/graal
/Library/Java/JavaVirtualMachines/graalvm-ce-java11-20.1.0/Contents/Home/bin/native-image -jar ./build/install/okurl-shadow/lib/okurl-*-all.jar --no-fallback -H:ResourceConfigurationFiles=resources.config -H:ReflectionConfigurationFiles=reflect.config,./build/tmp/kapt3/classes/main/META-INF/native-image/picocli-generated/reflect-config.json --enable-https build/graal/okurl
