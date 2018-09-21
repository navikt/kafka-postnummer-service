#!/usr/bin/env bash
set -e

make release
git push --tags https://$GITHUB_TOKEN@github.com/navikt/kafka-postnummer-service HEAD:master
