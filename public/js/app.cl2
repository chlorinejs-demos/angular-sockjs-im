(import! [:private "boot.cl2"])
(include-core!)

(def
 app
 (.. angular (module "myApp" ["myApp.filters" "myApp.directives"])))

(include! "./services.cl2")
(include! "./controllers.cl2")
(include! "./filters.cl2")
(include! "./directives.cl2")