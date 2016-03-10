from tornado.web import RequestHandler
from tornado.web import StaticFileHandler
from tornado.ioloop import IOLoop
from tornado.web import Application


class MainHandler(RequestHandler):
    def get(self, *args, **kwargs):
        self.render("templates/index.html")


if __name__ == "__main__":
    print "Bike Parking Mtl Server is started!"

    application = Application([
        (r"/", MainHandler),
        (r"/resources/(.*)", StaticFileHandler, {"path": "resources"})
    ])
    application.listen(8080)
    IOLoop.instance().start()
