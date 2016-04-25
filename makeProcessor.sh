#!/bin/bash

./gradlew :mhttp-compiler:build

cp ./mhttp-compiler/build/libs/mhttp-compiler.jar ./mhttp/libs/mhttp-compiler.jar

./gradlew :mhttp-annotations:build

cp ./mhttp-annotations/build/libs/mhttp-annotations.jar ./mhttp/libs/mhttp-annotations.jar

./gradlew compileDebugJava


