
docker-compose up --detach --wait

echo "Sleeping for 30 seconds while containers get ready ..."
sleep 30
echo "Back! Running tests"

k6 run \
  -e TEST_PROFILE=smoke \
  -e BASE_URL=http://localhost:8080 \
  ./k6/write-test.js

