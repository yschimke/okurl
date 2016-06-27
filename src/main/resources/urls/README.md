sucked in via https://api.apigee.com/v1/consoles/foursquare/apidescription?format=internal
list at https://api.apigee.com/v1/consoles/

$ curl 'https://api.apigee.com/v1/consoles/stackexchange/apidescription?format=internal' | jq -r '.application.endpoints[] | .base + .resources[].path' | perl -p -i -e 's/{format}/json/' | sort | uniq

