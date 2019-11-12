#!/usr/bin/env bash
set -e

CONFIG_FILE=./stub-trustframework-rp.yml

cd "$(dirname "$0")"

./gradlew installDist

./build/install/stub-trustframework-rp/bin/stub-trustframework-rp server $CONFIG_FILE
