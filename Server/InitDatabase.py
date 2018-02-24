# -*- coding: utf-8 -*-
# Init database with Montreal Open Data

import csv
import datetime
import unittest
import re
import requests
import json
import time

class BikeParking():
    def __init__(self):
        self.location = []
        self.capacity = 1
        self.status = 100
        self.availability_stats = 100
        self.id = 0
        self.added = int(time.time()*1000)
        self.last_update = self.added
        self.comment = ""


def get_capacity(line):
    matchObj = re.match(r'.* \(cp-([0-9]+)\)', line, re.M|re.I)
    capacity = 1
    if matchObj:
        capacity = int(matchObj.group(1))

    return capacity


def parse_csv_file(csv_filepath):
    bike_parkings = []
    with open(csv_filepath, newline='', encoding='utf-8') as csvfile:
        bike_parking_reader = csv.reader(csvfile)

        for row in bike_parking_reader:
            if row[0] == "INV_ID":
                continue
            bike_parking = BikeParking()
            bike_parking.id = row[0]
            if row[25] != "2":
                bike_parking.location = [float(row[25]), float(row[26])]
            else:
                bike_parking.location = [float(row[26]), float(row[27])]
            bike_parking.capacity = get_capacity(row[5])
            bike_parkings.append(bike_parking)

    return bike_parkings


def add_to_database(bike_parking, url, credentials=None):

    for parking in bike_parking:
        data = json.dumps(parking.__dict__)
        req = requests.post(url, data=data)


def mapping_database(url):
    geo_mapping = {
        "mappings": {
            "parking": {
                "properties": {
                    "location": {
                        "type": "geo_point"
                    },
                    "added": {
                       "type": "date"
                    },
                    "last_update": {
                      "type": "date"
                    }

                }
            }
        }
    }

    req = requests.put(url, data=json.dumps(geo_mapping))
    print("oki")







def test_get_capacity():
    capacity = get_capacity(u"Support à bicyclettes à haute densité 7 places (cp-7)")
    assert capacity == 7

def test_get


def change_status_bike_parking(status, url, credentials=None):
    server = couchdb.Server(url)
    couchdb.Resource.credentials = credentials
    bike_parking_db = server["bike_parking"]
    for id in bike_parking_db:
        doc = bike_parking_db.get(id)
        if doc.has_key("status"):
            doc["status"] = status
            bike_parking_db.save(doc)



if __name__ == "__main__":
    #mapping_database("http://localhost:9200/bike")
    #test_get_capacity()
    bike_parking = parse_csv_file("support_velo_sigs.csv")
    #print bike_parking
    add_to_database(bike_parking, "http://localhost:9200/bike/parking", ("supernovae", "Bonsai21"))
    #change_status_bike_parking(10, "http://192.99.54.190:5984/", ("supernovae", "Bonsai21"))
