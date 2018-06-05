
var Vue = require("vue")
var VueI18n = require("vue-i18n");

Vue.use(VueI18n);

const messages = {
  fr: {
      Languages: "Langages",
      Menu: "Menu",
      Radius_of_research: "Rayon de recherche",
      name: "nom",
      capacity: "capacité"
  },

  en: {
      Languages: "Languages"  ,
      Menu: "Menu",
      Radius_of_research: "Raduis of research",
      name: "name",
      capacity: "capacity"
  }

}

const i18n = new VueI18n({
  locale: 'en',
  messages,
});

exports.i18n = i18n;
