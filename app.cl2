(include! [:private "dev.cl2"])

(def routes (require "./routes"))
(include! "./routes/socket.cl2")

(def express (require "express"))
(def sockjs (require "sockjs"))
(def http (require "http"))

(def chat
  (.. sockjs
     (createServer
      {:sockjs_url "http://cdn.sockjs.org/sockjs-0.3.min.js"})))

(. chat (on "connection" on-connection))

(def app (express))
(def server (.. http (createServer app)))

(. chat (installHandlers server {:prefix "/chat"}))
(console.log " [*] Listening on 3000")
(. server (listen 3000))

(..
 app
 (configure
  (fn []
   (.. app (set "views" (+ __dirname "/views")))
   (.. app (set "view engine" "jade"))
   (.. app (set "view options" {:layout false}))
   (.. app (use (.. express bodyParser)))
   (.. app (use (.. express methodOverride)))
   (.. app (use (.. express (static (+ __dirname "/public")))))
   (.. app (use (-> app :router))))))

(..
 app
 (configure
  "development"
  (fn
   []
   (.. app
     (use
      (.. express
       (errorHandler {:showStack true, :dumpExceptions true})))))))

(..
 app
 (configure
  "production"
  (fn []  (. app (use (. express errorHandler))))))

(. app (get "/" (-> routes :index)))

(. app (get "/partials/:name" (-> routes :partials)))

;;(.. app (get "*" (-> routes :index)))
