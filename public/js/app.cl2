(include! [:private "dev.cl2"])

(defn serialize
  "Converts a message object to JSON strings so that it can be transfered
over the network."
  [msg]
  (.. JSON (stringify msg)))

(defn deserialize
  "Converts a serialized message back to object"
  [data]
  (.. JSON (parse data)))

(def app
 (.. angular (module "myApp" ["myApp.filters" "myApp.directives"])))

(include! "./services.cl2"
          "./controllers.cl2"
          "./filters.cl2"
          "./directives.cl2")
