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
   (if (!= id exclude)
     (.. (get clients i) (write (.. JSON (stringify message)))))))

(defn claim
  "Registers a new user-name"
  [new-name]
  (if-not (or (not new-name) (get claimed-names new-name))
    (set! (get claimed-names new-name) true)
    true))

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
  (broadcast {:type "new-user" :name (:name conn) :users (get-users)}
             (:id conn))
  (whisper
   (:id conn)
   {:name (:name conn) :type "welcome"})
  (whisper
   (:id conn)
   {:id (:id conn), :message buffer, :type "history"})
  (.. conn (on "data" on-data))
  (.. conn (on "close" on-close)))

(defn on-text
  "Handles text events"
  []
  (set!
   (-> data :message)
   (.. (-> data :message) (substr 0 128)))

  (if (> (count buffer) 15) (.. buffer shift))
  (.. buffer (push (-> data :message)))
  (broadcast
   {:id (:id conn),
    :message (-> data :message),
    :type "message"}))

(defn on-change-name
  "Handles on-change-name events"
  []
  (if (claim (:name data))
    (do
      (def old-name (:name data))
      (free old-name)
      (set! (:name conn) (:name data))
      (broadcast "change-name" {:new-name name, :old-name old-name}))))

(defn on-data
  "Handles messages when an on-data event happens"
  [data]
  ;; TODO: max data size?
  (set! data (deserialize data))
  (cond
   (and (== (-> data :type) "text")
        (-> data :message))
   (on-text)

   (== (:name data) "change-name")
   (on-change-name)))

(defn on-close
  "Handles close connection events"
  []
  (free-name (:name conn))
  (delete (get clients (:id conn)))
  (broadcast {:type "user-left" :name (:name conn)}))
