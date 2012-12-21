(import! [:private "boot.cl2"])

(def express (require "express"))
(def routes (require "./routes"))
(def socket (require "./routes/socket.js"))

(def app (express))
(def http (require "http"))
(def server (.. http (createServer app)))
(def io (.. (require "socket.io") (listen server)))

(.. server (listen 3000))

(..
 app
 (configure
  (fn
   []
   (do
    (.. app (set "views" (+ __dirname "/views")))
    (.. app (set "view engine" "jade"))
    (.. app (set "view options" {:layout false}))
    (.. app (use (.. express bodyParser)))
    (.. app (use (.. express methodOverride)))
    (.. app (use (.. express (static (+ __dirname "/public")))))
    (.. app (use (-> app :router)))
    undefined))))

(..
 app
 (configure
  "development"
  (fn
   []
   (do
    (..
     app
     (use
      (..
       express
       (errorHandler {:showStack true, :dumpExceptions true}))))
    undefined))))

(..
 app
 (configure
  "production"
  (fn [] (do (.. app (use (.. express errorHandler))) undefined))))

(.. app (get "/" (-> routes :index)))

(.. app (get "/partials/:name" (-> routes :partials)))

(.. app (get "*" (-> routes :index)))

(.. (-> io :sockets) (on "connection" socket))

(..
 server
 (listen
  3000
  (fn
   []
   (do
    (..
     console
     (log
      "Express server listening on port %d"
      (-> (.. server address) :port)))
    undefined))))
