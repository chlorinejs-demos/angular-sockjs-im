(import! [:private "boot.cl2"])
(include-core!)

(defn serialize
  "Converts a message object to JSON strings so that it can be transfered
over the network."
  [msg]
  (.. JSON (stringify msg)))

(defn deserialize
  "Converts a serialized message back to object"
  [data]
  (.. JSON (parse data)))

(def
 app
 (.. angular (module "myApp" ["myApp.filters" "myApp.directives"])))

(include! "./services.cl2")
(include! "./controllers.cl2")
(include! "./filters.cl2")
(include! "./directives.cl2")