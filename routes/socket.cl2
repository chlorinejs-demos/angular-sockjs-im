;(import! [:private "boot.cl2"])

(def buffer [])
(def ^{:doc "Stores client's connections currently in use"}
  clients {})
(def ^{:doc "User-id counter used in generating guest names"}
  next-user-id 1)
(def ^{:doc "Stores user-names currently in use"}
  claimed-names {})

(defn serialize
  "Converts a message object to JSON strings so that it can be transfered
over the network."
  [msg]
  (.. JSON (stringify msg)))

(defn deserialize
  "Converts a serialized message back to object"
  [data]
  ;;TODO: error handler
  (.. JSON (parse data)))

(defn whisper
  "Sends messages to a single client"
 [id message]
 (if (not (get clients id))
   (.. (get clients id)
       (write (serialize message)))))

(defn broadcast
  "Sends messages to many clients. An excluded client can be specified"
  [message exclude]
  (dokeys [id clients]
   (if (not= id exclude)
     (.. (get clients id) (write (.. JSON (stringify message)))))))

(defn claim
  "Registers a new user-name"
  [new-name]
  (if-not (or (not new-name) (get claimed-names new-name))
    (do (set! (get claimed-names new-name) true)
        true)))

(defn gen-guest-name
  "Generates a unique guest name for each newcomer."
  []
  (def new-name)
  (do-while (not (claim new-name))
            (do
              (set! new-name (+* "Guest " next-user-id))
              (inc! next-user-id)))
  new-name)

(defn free-name
  "Removes an username from claimed names when connection closes
or user changes his/her name."
  [client-name]
  (if (get claimed-names client-name)
    (delete (get claimed-names client-name))))

(defn get-users
  "Returns list of current users."
  []
  (def users [])
  (dokeys [user claimed-names]
          (.. users (push user)))
  users)

(defn on-connection
  "Handles connections."
  [conn]
  (set! (:name conn) (gen-guest-name))
  (set! (get clients (:id conn)) conn)
  (console.log "Fire in the hole!" (:id conn) (:name conn) claimed-names)
  (broadcast {:type "new-user" :name (:name conn) :users (get-users)} ;;in use?
             (:id conn))
  (whisper
   (:id conn)
   {:type "init"
    :name (:name conn) :users (get-users)})
  (whisper
   (:id conn)
   {:id (:id conn), :message buffer, :type "history"})
  (.. conn (on "data" #(on-data % conn)))
  (.. conn (on "close" #(on-close conn))))

(defn on-change-name
  "Handles on-change-name events"
  [data conn]
  (if (claim (:name data))
    (do
      (def old-name (:name data))
      (free-name old-name)
      (set! (:name conn) (:name data))
      (broadcast {:type "change-name"
                  :new-name (:name data)
                  :old-name old-name}))))

(defn on-data
  "Handles messages when an on-data event happens"
  [data conn]
  ;; TODO: max data size?
  (console.log "Yummy... got some data" (type data))
  (set! data (deserialize data))
  (console.log "Good, let's see" (:type data))
  (console.log data)
  (cond
   (and (== (-> data :type) "text")
        (-> data :message))
   (on-text data conn)

   (and (== (:type data) "change-name")
        (:name data))
   (on-change-name data conn)))

(defn on-text
  "Handles text events"
  [data conn]
  (console.log "Got some text. Have fun!")
  (set!
   (-> data :message)
   (.. (-> data :message) (substr 0 128)))

  (if (> (count buffer) 15) (.. buffer shift))
  (.. buffer (push (-> data :message)))
  (broadcast
   {:name (:name conn),
    :message (-> data :message),
    :type "text"}))

(defn on-close
  "Handles close connection events"
  [conn]
  (free-name (:name conn))
  (delete (get clients (:id conn)))
  (broadcast {:type "user-left" :name (:name conn)})
  (console.log clients claimed-names))
