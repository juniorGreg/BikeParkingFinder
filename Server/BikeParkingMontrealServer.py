from tornado.web import RequestHandler
from tornado.web import StaticFileHandler
from tornado.ioloop import IOLoop
from tornado.web import Application
import couchdb
import json
import math
import datetime

CIRC_EARTH = 40075016.68557849

class BikeParking():
    def __init__(self):
        self.latitude = 0.0
        self.longitude = 0.0
        self.capacity = 1
        self.status = 100
        self.id = 0
        self.added = str(datetime.datetime.now())
        self.last_update = self.added


def meter_to_deg(radius):
        return 360.0*radius/CIRC_EARTH

def is_in_radius(coord_initial, coord, radius):
    radius_sqr = math.pow(radius, 2.0)
    distance_sqr = math.pow(coord[0]-coord_initial[0], 2.0)+math.pow(coord[1]-coord_initial[1], 2.0)
    return radius_sqr > distance_sqr


class BikeParkingQueryHandler(RequestHandler):
    def initialize(self):
        couchdb.Resource.credentials =  ("supernovae", "Bonsai21")
        self.client = couchdb.Server("http://192.99.54.190:5984/")
        self.bike_parking_db = self.client["bike_parking"]

    def get(self, query):
        if query == "geolocation":
            coord = json.loads(self.get_argument("coord", "[45.5077, -73.544]"))
            radius = float(self.get_argument("radius", "350"))
            radius_deg = meter_to_deg(radius)
            startkey = coord[0]-radius_deg
            endkey = coord[0]+radius_deg

            geolocation = self.bike_parking_db.view("bike_parking_query/geolocation", startkey=startkey, endkey=endkey)
            bike_parking_location = map(lambda x: x.value, geolocation.rows)
            filter_bike_parking_location = filter(lambda x: is_in_radius(coord, x["coord"], radius_deg), bike_parking_location)
            self.write(json.dumps(filter_bike_parking_location))

        elif query == "add_bike_parking":
            coord = json.loads(self.get_argument("coord"))
            radius = float(self.get_argument("radius", "8.0"))
            capacity = int(self.get_argument("capacity", "1"))
            radius_deg = meter_to_deg(radius)
            startkey = coord[0]-radius_deg
            endkey = coord[0]+radius_deg

            geolocation = self.bike_parking_db.view("bike_parking_query/geolocation", startkey=startkey, endkey=endkey)
            bike_parking_location = map(lambda x: x.value, geolocation.rows)
            filter_bike_parking_location = filter(lambda x: is_in_radius(coord, x["coord"], radius_deg), bike_parking_location)
            if len(filter_bike_parking_location) == 0:
                bike_parking = BikeParking()
                bike_parking.latitude = coord[0]
                bike_parking.longitude = coord[1]
                bike_parking.capacity = capacity
                self.bike_parking_db.save(bike_parking.__dict__)

            elif len(filter_bike_parking_location) == 1:
                bike_parking = filter_bike_parking_location[0]
                status = bike_parking["status"]
                if status < 100:
                    status += 1
                    bike_parking["status"] = status
                    self.bike_parking_db.update(bike_parking)

        elif query == "remove_bike_parking":
            _id = self.get_argument("id")

            bike_parking = self.bike_parking_db.get(_id)
            status = bike_parking["status"]
            status -= 1

            if status <= 0:
                self.bike_parking_db.delete(_id)
            else:
                bike_parking["status"] = status
                self.bike_parking_db.update(bike_parking)

            self.bike_parking_db.update(bike_parking)
            self.write(json.dumps(bike_parking))

        elif query == "confirm_bike_parking":
            _id = self.get_argument("id")

            bike_parking = self.bike_parking_db.get(_id)
            bike_parking["status"] += 1

            self.bike_parking_db.update(bike_parking)
            self.write(json.dumps(bike_parking))

        elif query == "all_locations":
            geolocation = self.bike_parking_db.view("bike_parking_query/geolocation")
            bike_parking_location = map(lambda x: x.value, geolocation.rows)
            self.write(json.dumps(bike_parking_location))


class MainHandler(RequestHandler):
    def get(self, *args, **kwargs):
        self.render("templates/index.html")


if __name__ == "__main__":
    print "Bike Parking Mtl Server is started!"

    application = Application([
        (r"/", MainHandler),
        (r"/bike_parking/(.*)", BikeParkingQueryHandler),
        (r"/resources/(.*)", StaticFileHandler, {"path": "resources"})
    ])
    application.listen(8080)
    IOLoop.instance().start()
