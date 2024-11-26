#!/bin/sh

set -e

exec java -jar /tmp/codecrafters-build-redis-java/codecrafters-redis.jar "$@"