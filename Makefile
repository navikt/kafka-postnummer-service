DOCKER  := docker
GRADLE  := ./gradlew
VERSION := $(shell cat ./VERSION)

.PHONY: all build test docker docker-push bump-version release

all: build test docker
release: tag docker-push

build:
	$(GRADLE) installDist

test:
	$(GRADLE) check

docker:
	$(DOCKER) build --pull -t navikt/postnummer-service -t navikt/postnummer-service:$(VERSION) .

docker-push:
	$(DOCKER) push navikt/postnummer-service:$(VERSION)

bump-version:
	@echo $$(($$(cat ./VERSION) + 1)) > ./VERSION

tag:
	git add VERSION
	git commit -m "Bump version to $(VERSION) [skip ci]"
	git tag -a $(VERSION) -m "auto-tag from Makefile"
