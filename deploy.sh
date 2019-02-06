#!/bin/sh

echo "Building App..."
sbt fullOptJS::webpack

echo "Copying the App Bundle to public dir..."
cp target/scala-2.12/scalajs-bundler/main/kgv-prototype-opt-bundle.js public/kgv/js/

echo "Uploading the App to the Solid Server..."
curl --cookie "connect.sid=$SOLID_COOKIE" --upload-file index.html https://spoterme.solid.community/index.html
curl --cookie "connect.sid=$SOLID_COOKIE" --upload-file public/kgv/css/app.css https://spoterme.solid.community/public/kgv/css/app.css
curl --cookie "connect.sid=$SOLID_COOKIE" --upload-file public/kgv/js/kgv-prototype-opt-bundle.js https://spoterme.solid.community/public/kgv/js/kgv-prototype-opt-bundle.js
curl --cookie "connect.sid=$SOLID_COOKIE" --upload-file public/kgv/images/image-1.svg https://spoterme.solid.community/public/kgv/images/image-1.svg
curl --cookie "connect.sid=$SOLID_COOKIE" --upload-file public/kgv/images/image-2.svg https://spoterme.solid.community/public/kgv/images/image-2.svg
curl --cookie "connect.sid=$SOLID_COOKIE" --upload-file public/kgv/images/image-3.svg https://spoterme.solid.community/public/kgv/images/image-3.svg
