(require ["./routes"]
         ["express"]
         ["http"]
         ["sockjs"])

(load-file "./node_modules/cl2-contrib/src/concurrency.cl2")
(load-file "./node_modules/cl2-contrib/src/json.cl2")
(load-file "./node_modules/cl2-contrib/src/timers.cl2")
(load-file "./node_modules/socket-cl2/src/server.cl2")

(load-file "./routes/socket.cl2")

(def chat
  (. sockjs
     (createServer
      {:websocket false
       :sockjs_url "http://cdn.sockjs.org/sockjs-0.3.min.js"})))

(defsocket chat
  {:on-open
   (fn [send-response conn]
     (set! conn.name (gen-guest-name))
     (swap! socket-clients #(assoc % conn.id conn))
     (broadcast :new-user {:name (:name conn)
                           :users (get-users)}
                [(:id conn)])
     (send-response :init {:name (:name conn)
                           :users (get-users)}))})

(defsocket-handler :change-name
  (fn [_ data send-response conn]
    (if (claim (:name data))
      (let [old-name (:name conn)]
        (free-name old-name)
        (set! conn.name (:name data))
        (broadcast :change-name
                   {:new-name (:name data)
                    :old-name old-name})))))

(defsocket-handler :init
  (fn [_ _ send-response conn]
    (send-response
     :init
     {:name (:name conn) :users (get-users)})))

(defsocket-handler :text
  (fn [_ data send-response conn]
    (console.log "Got some text. Have fun!")
    (set! data.message
          (.. data.message (substr 0 128)))

    (broadcast
     :text {:name (:name conn),
            :message (:message data)}
     [(:id conn)])))

(defsocket-handler :close
  (fn [_ data send-response conn]
    (free-name (:name conn))
    (broadcast :user-left {:name (:name conn)})
    (console.log @socket-clients claimed-names)))

(def app (express))
(def server (. http (createServer app)))

(. chat (installHandlers server {:prefix "/chat"}))

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
     (.use (. express (static (+ __dirname "/public"))))
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
