
var Vue = require("vue")
var VueI18n = require("vue-i18n");

Vue.use(VueI18n);

const messages = {
  fr: {
    message: {
      Languages: "Langages"
    }
  },

  en: {
    message: {
      Languages: "Languages"
    }
  }

}

const i18n = new VueI18n({
  locale: 'en',
  messages,
});

exports.i18n = i18n;
