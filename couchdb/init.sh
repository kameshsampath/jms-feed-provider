#!/bin/bash

set -ex

/docker-entrypoint.sh /opt/couchdb/bin/couchdb &

# wait for couchdb to be up and running
TIMEOUT=0
echo "wait for CouchDB to be up and running"
until $( curl --output /dev/null --silent http://$DB_HOST:$DB_PORT/_utils ) || [ $TIMEOUT -eq 30 ]; do
echo "waiting for CouchDB to be available"

sleep 2
let TIMEOUT=TIMEOUT+1
done

if [ $TIMEOUT -eq 30 ]; then
echo "failed to setup CouchDB"
exit 1
fi

curl -X PUT -u "${COUCHDB_USER}:${COUCHDB_PASSWORD}" http://${DB_HOST}:${DB_PORT}/_users

curl -X PUT -u "${COUCHDB_USER}:${COUCHDB_PASSWORD}" http://${DB_HOST}:${DB_PORT}/_replicator

curl -X PUT -u "${COUCHDB_USER}:${COUCHDB_PASSWORD}" http://${DB_HOST}:${DB_PORT}/_global_changes

echo "successfully setup and configured CouchDB for OpenWhisk"

sleep inf