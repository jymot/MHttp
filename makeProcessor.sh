#!/bin/bash

./gradlew :processor:build

cp ./processor/build/libs/processor.jar ./mhttp/libs/processor.jar

./gradlew compileDebugJava


