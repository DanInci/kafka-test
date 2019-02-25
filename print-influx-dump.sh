#!/usr/bin/env bash

command curl -G 'http://localhost:8086/query?pretty=true&u=influxuser&p=qwerty' --data-urlencode "db=filtered_models" --data-urlencode "q=SELECT * FROM consumer_data"
