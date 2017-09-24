.PHONY: clean build dist test docker-image tag-version push-latest push-version

SHELL = /bin/bash

VERSION := $(shell cat build.sbt | sed -n -E -e 's/version[[:space:]]+:=[[:space:]]+"([^"]+)"/\1/p')

NAME := $(shell cat build.sbt | sed -n -E -e 's/name[[:space:]]+:=[[:space:]]+"([^"]+)"/\1/p')

REPO = gabbi

IMAGE_LATEST=$(REPO)/$(NAME):latest

IMAGE_VERSION=$(REPO)/$(NAME):$(VERSION)

clean:
	sbt clean
build:
	sbt compile
dist:
	sbt assembly
test:
	sbt test
docker-image:
	docker build -t $(IMAGE_LATEST) --rm .
tag-version:
	docker tag $(IMAGE_LATEST) $(IMAGE_VERSION)
push-latest:
	docker push $(IMAGE_LATEST)
push-version:
	docker push $(IMAGE_VERSION)
