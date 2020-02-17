#!/usr/bin/env bash
set -e

CONFIG_FILE=./stub-trustframework-rp.yml
export SERVICE_PROVIDER_URI=http://localhost:2210
export APPLICATION_PORT=4410
export ADMIN_PORT=4411
export TRUSTFRAMEWORK_RP=http://localhost:4410
export RP=dbs
export IS_USING_SERVICE_PROVIDER=true
export CONTRACTED_IDP_URI=http://localhost:3333/sign-up
LOCAL_IP="$(ipconfig getifaddr en0)"
export REDIS_URI="redis://${LOCAL_IP}:6380"
log="logs/rp1_console.log"

cd "$(dirname "$0")"

./gradlew installDist

  CID=$(docker ps -q -f status=running -f name=clientRedis)
  if [ ! "${CID}" ]; then
      echo "Starting client redis"
      docker run --name clientRedis -d -p 6380:6379 --rm redis
  fi

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
