#!/bin/bash
# Part of NDLA oembed_proxy.
# Copyright (C) 2016 NDLA
#
# See LICENSE

VERSION="$1"
source ./build.properties
PROJECT="$NDLAOrganization/$NDLAComponentName"

if [ -z $VERSION ]
then
    VERSION="SNAPSHOT"
fi

sbt -Ddocker.tag=$VERSION docker
echo "BUILT $PROJECT:$VERSION"