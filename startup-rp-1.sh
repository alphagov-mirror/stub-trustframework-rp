#!/usr/bin/env bash
set -e

CONFIG_FILE=./stub-trustframework-rp.yml
export STUB_BROKER_URI=http://localhost:6610
export APPLICATION_PORT=4410
export ADMIN_PORT=4411
export TRUSTFRAMEWORK_RP=http://localhost:4410/response

cd "$(dirname "$0")"

./gradlew installDist

./build/install/stub-trustframework-rp/bin/stub-trustframework-rp server $CONFIG_FILE
