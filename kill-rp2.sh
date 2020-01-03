#!/usr/bin/env bash
set -e

  if [ -f "./tmp/pids/rp2.pid" ]; then
    echo "Killing rp2"
    kill "$(< ./tmp/pids/rp2.pid)" || true
    rm -f ./tmp/pids/rp2.pid
  fi
