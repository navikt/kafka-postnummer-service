#!/usr/bin/env bash
set -ev

if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_BRANCH}" = "master" ]; then
    make release
    git push --tags https://${GITHUB_TOKEN}@github.com/navikt/kafka-postnummer-service.git HEAD:master
fi
