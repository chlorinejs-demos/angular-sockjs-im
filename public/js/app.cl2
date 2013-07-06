(load-file "../../node_modules/cl2-contrib/src/concurrency.cl2")
(load-file "../../node_modules/cl2-contrib/src/timers.cl2")
(load-file "../../node_modules/angular-cl2/src/angular.cl2")
(load-file "../../node_modules/socket-cl2/src/client.cl2")

(def sockjs-url (+* window.location.protocol "//"
                    window.location.host
                    "/chat"))

(def name (atom ""))
(def users (atom []))
(def messages (atom []))

(defn serialize
  "Converts a message map to a JSON string so that it can be transfered
over the network."
  [msg]
  (. JSON (stringify msg)))

(defn deserialize
  "Converts a serialized message back to a map"
  [data]
  (. JSON (parse data)))

(defn scope-change-name
  "Updates scope when someone changes his/her name. Helper function of
  `scope-data-handler`."
  [old-name new-name]
  (swap! users
         (fn [coll]
           (conj (filter #(not= % old-name) coll)
                 new-name)))
  (if (=== old-name @name)
    (reset! name new-name))

  (swap! messages
         #(concat % [{:text (+* "User " old-name " is now known as "
                                 new-name),
                       :user "chatroom"}])))

(defapp myApp [])

(defcontroller AppCtrl
  [$scope socket]
  ;; Messages are stored in a vector. Each message is a map with
  ;; the form of {:user "the-user-who-sent-the-msg" :text "msg-content"}.
  ;; ;user can be `chatroom` (aka the server)
  ($->atom name name)
  ($->atom users users)
  ($->atom messages messages)

  (defn$ changeName
    "When a user changes his own name, sends that to server via sockjs.
  If server responds true which means the new name was accepted,
  clears `newName` box, otherwise alerts the user."
    []
    (if (. socket (emit :change-name {:name ($- newName)}))
      (def$ newName "")
      (alert "There was an error changing your name")))

  (defn$ sendMessage
    "Does some tasks when a user clicks to send his message away:
  - sends the message to server via sockjs
  - adds that message to global messages (hence the `Messages` log gets
  updated)
  - clears the `Message` box"
    []
    (. socket (emit :text {:message ($- message)}))
    (swap! messages
           #(concat % [{:text ($- message) :user ($- name)}]))
    (def$ message "")))

(defservice socket
  []
  (def sock (SockJS. sockjs-url nil
                     {:protocols_whitelist
                      ['xhr-polling]}))
  (defsocket sock {:load-balancer-workaround? true})
  (defsocket-handler :init
    (fn [_ data]
      (console.log "initializing... Go!")
      (reset! name  data.name)
      (reset! users data.users)))

  (defsocket-handler :text
    (fn [_ data]
      (swap! messages
             #(concat % [{:text data.message, :user data.name}]))))

  (defsocket-handler :change-name
    (fn [_ data]
      (scope-change-name (:old-name data) (:new-name data))))

  (defsocket-handler :new-user
    (fn [_ data]
      (swap! messages
             #(concat % [{:text (+ "User " data.name " has joined."),
                          :user "chatroom"}]))
      (swap! users
             #(concat % [data.name]))))

  (defsocket-handler :user-left
    (fn [_ data]
      (swap! messages
             #(concat % [{:text (+ "User " data.name " has left."),
                          :user "chatroom"}]))

      (swap! users
             (fn [coll]
               (remove #(= data.name %)
                       coll)))))
  ;; returns the modified sockjs instance
  sock)
