#!/usr/bin/env bash
set -e

  if [ -f "./tmp/pids/rp1.pid" ]; then
    echo "Killing rp1"
    kill "$(< ./tmp/pids/rp1.pid)" || true
    rm -f ./tmp/pids/rp1.pid
  fi
