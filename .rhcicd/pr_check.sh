#!/bin/bash

set -exv

source ./.rhcicd/pr_check_backend.sh
source ./.rhcicd/pr_check_engine.sh
docker build . -f docker/Dockerfile.notifications-aggregator.jvm
docker build . -f docker/Dockerfile.notifications-camel-demo-log.jvm

# Until test results produce a junit XML file, create a dummy result file so Jenkins will pass
mkdir -p $WORKSPACE/artifacts
cat << EOF > ${WORKSPACE}/artifacts/junit-dummy.xml
<testsuite tests="1">
    <testcase classname="dummy" name="dummytest"/>
</testsuite>
EOF
