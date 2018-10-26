#!/bin/sh -ex

./gradlew -q clean test distTar
./gradlew clean

git co master
git pull

VERSION="$1"
TAG_VERSION="${VERSION}.0"
BRANCH="release/$VERSION"

git co -b "$BRANCH"
git push origin "$BRANCH"

RELEASE_BODY=$(cat <<EOF
{
  "tag_name": "${TAG_VERSION}",
  "target_commitish": "${BRANCH}",
  "name": "${TAG_VERSION}",
  "body": "Release ${TAG_VERSION}",
  "draft": false,
  "prerelease": false
}
EOF
)

RELEASE_ID=$(okurl -d "$RELEASE_BODY" https://api.github.com/repos/yschimke/okurl/releases | jq .id)

echo Created "https://api.github.com/repos/yschimke/okurl/releases/${RELEASE_ID}"

./gradlew -q clean distTar bintrayUpload

git tag "$TAG_VERSION"
git push origin "$TAG_VERSION"

./okurl -H "Content-Type: application/x-gzip" -d "@build/distributions/okurl-${TAG_VERSION}.tgz" "https://uploads.github.com/repos/yschimke/okurl/releases/${RELEASE_ID}/assets?name=okurl-${TAG_VERSION}.tgz" | jq ".browser_download_url"

