#!/bin/bash

./gradlew :processor:build

cp ./processor/build/libs/processor.jar ./library/libs/processor.jar

./gradlew compileDebugJava


