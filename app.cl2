(require ["./routes"]
         ["express"]
         ["http"]
         ["sockjs"]
         ["gzippo"])

(load-file "socket-cl2/src/server.cl2")
(load-file "./routes/socket.cl2")

(defsocket chat
  (. sockjs
     (createServer
      {:websocket false
       :sockjs_url "http://cdn.sockjs.org/sockjs-0.3.min.js"}))
  { ;; :debug true
   :on-open
   (fn [respond conn]
     (set! conn.name (gen-guest-name))
     (swap! socket-clients #(assoc % conn.id conn))
     (.broadcast chat :new-user {:name (:name conn)
                                 :users (get-users)}
                 [(:id conn)])
     (respond :init {:name (:name conn)
                     :users (get-users)}))
   :on-close
   (fn [conn]
     (free-name (:name conn))
     (.broadcast chat :user-left {:name (:name conn)}))})

(.on chat :change-name
     (fn [_ data send-response conn]
       (if (claim (:name data))
         (let [old-name (:name conn)]
           (free-name old-name)
           (set! conn.name (:name data))
           (.broadcast chat :change-name
                       {:new-name (:name data)
                        :old-name old-name})))))

(.on chat :init
  (fn [_ _ respond conn]
    (respond
     :init
     {:name (:name conn) :users (get-users)})))

(.on chat :text
     (fn [_ data respond conn]
       (set! data.message
             (.. data.message (substr 0 128)))

       (.broadcast chat
                   :text {:name (:name conn),
                          :message (:message data)}
                   [(:id conn)])))

(def app (express))
(def server (. http (createServer app)))

(.install chat server {:prefix "/chat"})

(println " [*] Listening on 3000")
(. server (listen 3000))

(.
 app
 (configure
  #(doto app
     (.set "views" (+ __dirname "/views"))
     (.set "view engine" "jade")
     (.set "view options" {:layout false})
     (.use (. express bodyParser))
     (.use (. express methodOverride))
     (.use (. gzippo (staticGzip (+ __dirname "/public"))))
     (.use (:router app)))))

(doto app
  (.configure
   "development"
   #(. app (use (. express (errorHandler {:showStack true,
                                          :dumpExceptions true})))))
  (.configure
   "production"
   #(. app (use (. express errorHandler))))
  (.get "/" (-> routes :index))
  (.get "/partials/:name" (:partials routes))
  (.get "*" (-> routes :index)))
