from tornado.web import RequestHandler
from tornado.web import StaticFileHandler
from tornado.ioloop import IOLoop
from tornado.web import Application
import couchdb
import json

CIRC_EARTH = 40075016.68557849


def meter_to_deg(radius):
        return 360.0*radius/CIRC_EARTH


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
            startkey = [coord[0]-radius_deg, coord[1]-radius_deg]
            endkey = [coord[0]+radius_deg, coord[1]+radius_deg]

            geolocation = self.bike_parking_db.view("bike_parking_query/geolocation", startkey=startkey, endkey=endkey)
            bike_parking_location = map(lambda x: x.key, geolocation.rows)
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
