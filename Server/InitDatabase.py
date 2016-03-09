# Init database with Montreal Open Data

import couchdb
import csv


class BikeParking():
    def __init__(self):
        self.latitude = 0.0
        self.longitude = 0.0
        self.number_place = 1
        self.status = 100
        self.id = 0


def parse_csv_file(csv_filepath):
    bike_parkings = []
    with open(csv_filepath) as csvfile:
        bike_parking_reader = csv.reader(csvfile, delimiter=',', quotechar='|')

        for row in bike_parking_reader:
            bike_parking = BikeParking()
            bike_parking.id = row[0]
            bike_parking.longitude = row[25]
            bike_parking.latitude = row[26]
            bike_parkings.append(bike_parking)

    return bike_parkings


def add_to_database(bike_parking, url, credentials=None):
    pass


if __name__ == "__main__":
    bike_parking = parse_csv_file("support_velo_sigs.csv")
    print bike_parking
