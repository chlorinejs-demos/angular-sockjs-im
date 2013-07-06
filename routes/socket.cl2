(def ^{:doc "User-id counter used in generating guest names"}
  next-user-id 1)
(def ^{:doc "Stores user-names currently in use"}
  claimed-names {})

(defn claim
  "Registers a new user-name"
  [new-name]
  (when-not (or (not new-name) (get claimed-names new-name))
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
  (keys claimed-names))
