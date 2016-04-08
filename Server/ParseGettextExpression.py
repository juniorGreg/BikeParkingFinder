import re
import urllib


def get_gettextexp(text):
    m = re.findall('_\(\"([a-z_A-Z .]*)\"\)', text)
    if m:
        return set(m)


def save_pot(url, expressions):
    with open(url, "w") as stream:
        for expression in expressions:
            stream.write('msgid "'+expression+'"\n')
            stream.write('msgstr ""\n\n')

if __name__ == '__main__':
    text = '{{_("Your location accuracy is too low.")}}'
    text = urllib.urlopen("templates/index.html").read()

    expressions = get_gettextexp(text)
    save_pot("locale/BikeParkingMontrealServer.pot", expressions)