from tornado.web import RequestHandler
from visualcaptcha import Session, Captcha
from torndsession.sessionhandler import SessionBaseHandler


class VisualCaptchaRequestHanlder(SessionBaseHandler):

    def get(self, cmd, arg=None):
        if cmd == "start":

            visualCaptcha = Captcha(Session(self.session.session))

            visualCaptcha.generate(int(arg))
            self.write(visualCaptcha.getFrontendData())

        elif cmd == "audio":
            visualCaptcha = Captcha(Session(self.session.session))

            headers = {}
            result = visualCaptcha.streamAudio(headers, 'mp3')
            self.write(result)

        elif cmd == "image":
            visualCaptcha = Captcha(Session(self.session.session))

            headers = {}
            result = visualCaptcha.streamImage(headers, int(arg))
            self.write(result)

    def post(self, cmd):
        if cmd == "try":
            if self.trySubmission():
                self.write("ok")

    def trySubmission(self):
        visualCaptcha = Captcha(Session(self.session.session))
        frontendData = visualCaptcha.getFrontendData()
        if frontendData['imageFieldName'] in self.request.arguments:
            imageFieldName = self.request.arguments[frontendData['imageFieldName']][0]
            return visualCaptcha.validateImage(imageFieldName)
