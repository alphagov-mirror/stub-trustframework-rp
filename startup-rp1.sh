#!/usr/bin/env bash
set -e

CONFIG_FILE=./stub-trustframework-rp.yml
export STUB_BROKER_URI=http://localhost:6610
export APPLICATION_PORT=4410
export ADMIN_PORT=4411
export TRUSTFRAMEWORK_RP=http://localhost:4410/response
export RP=dbs
log="logs/rp1_console.log"

cd "$(dirname "$0")"

./gradlew installDist

PID_DIR=./tmp/pids
if [ ! -d $PID_DIR ]; then
    echo -e 'Creating PIDs directory\n'
    mkdir -p $PID_DIR
fi

LOGS_DIR=./logs
if [ ! -d $LOGS_DIR ]; then
  echo -e 'Creating LOGs directory\n'
  mkdir -p $LOGS_DIR
fi

./build/install/stub-trustframework-rp/bin/stub-trustframework-rp server $CONFIG_FILE &
  echo $! > ./tmp/pids/rp1.pid
