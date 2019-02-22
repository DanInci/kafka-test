#!/usr/bin/env bash

# wrapper for docker-compose
# all arguments are passed to docker-compose up

set -eux -o pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

# stop the services and clean up docker images
function clean() {
    set +e
    if [ -z "${prog_args}" ] || [ -n "${prog_args/*-d*/}" ]; then
        command docker-compose down -v
    fi
    docker image prune -f
}

prog_args="${*:-}"
trap clean exit

"${RUN:-true}" || exit 0

declare -a deps=(zookeeper kafka-1 kafka-2)

test ${#deps[@]} -eq 0 || docker-compose pull "${deps[@]}"
docker-compose build --pull
if [ ${#} -gt 0 ]; then
    command docker-compose up "${@}"
else
    command docker-compose up
fi
