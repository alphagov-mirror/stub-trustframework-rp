#!/usr/bin/env bash
set -e

CONFIG_FILE=./stub-trustframework-rp.yml
export STUB_BROKER_URI=http://localhost:5510
export APPLICATION_PORT=4412
export ADMIN_PORT=4413
export TRUSTFRAMEWORK_RP=http://localhost:4412/response
export RP=dwp

cd "$(dirname "$0")"

./gradlew installDist

./build/install/stub-trustframework-rp/bin/stub-trustframework-rp server $CONFIG_FILE
