(require ["./routes"]
         ["express"]
         ["http"]
         ["sockjs"])

(load-file "./routes/socket.cl2")

(def chat
  (. sockjs
     (createServer
      {:websocket false
       :sockjs_url "http://cdn.sockjs.org/sockjs-0.3.min.js"})))

(. chat (on "connection" on-connection))

(def app (express))
(def server (. http (createServer app)))

(. chat (installHandlers server {:prefix "/chat"}))

(console.log " [*] Listening on 3000")
(. server (listen 3000))

(borrow-macros doto)

(.
 app
 (configure
  #(doto app
     (.set "views" (+ __dirname "/views"))
     (.set "view engine" "jade")
     (.set "view options" {:layout false})
     (.use (. express bodyParser))
     (.use (. express methodOverride))
     (.use (. express (static (+ __dirname "/public"))))
     (.use (:router app)))))

(. app
   (configure
    "development"
    #(. app
        (use
         (. express
            (errorHandler {:showStack true, :dumpExceptions true}))))))

(. app
   (configure
    "production"
    #(. app (use (. express errorHandler)))))

(. app (get "/" (-> routes :index)))

(. app (get "/partials/:name" (:partials routes )))

;;(.. app (get "*" (-> routes :index)))
