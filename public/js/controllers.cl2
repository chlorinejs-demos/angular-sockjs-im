(defn AppCtrl [$scope socket]
  (set! (-> $scope :messages) [])
  (set! (:onmessage socket)
        (fn [message]
          (def data (deserialize (:data message)))
          (console.log "Got this msg: " data)
          (case (:type data)
            "init"
            (do
              (console.log "initializing... Go!")
              (set! (-> $scope :name) (-> data :name))
              (set! (-> $scope :users) (-> data :users))
              break)

            "text"
            (do (.. (-> $scope :messages)
                    (push {:text (-> data :message), :user (-> data :name)}))
                break)

            "change-name"
            (do (change-name (-> data :old-name) (-> data :new-name))
                break)

            "new-user"
            (do
              (..
               (-> $scope :messages)
               (push
                {:text (+ "User " (-> data :name) " has joined."),
                 :user "chatroom"}))
              (.. (-> $scope :users) (push (-> data :name)))
              break)

            "user-left"
            (do
              (..
               (-> $scope :messages)
               (push
                {:text (+ "User " (-> data :name) " has left."),
                 :user "chatroom"}))
              (set! (:users $scope)
                    (filter #(not= (:name data))
                            (:users $scope)))
              break)
            )))

  (defn
    change-name
    [old-name new-name]
    (console.log "Before, users: " (-> $scope :users))
    (console.log "and old name is: " old-name)
    (set! (-> $scope :users)
          (conj (filter #(not= % old-name) (-> $scope :users))
                new-name))
    (console.log "After, users: " (-> $scope :users))
    (..
     (-> $scope :messages)
     (push
      {:text (+ "User " old-name " is now known as " new-name "."),
       :user "chatroom"}))
    )
  (set!
   (-> $scope :changeName)
   (fn []
     (if (not (.. socket (emit {:type "change-name"
                                :name (-> $scope :newName)})))
       (alert "There was an error changing your name")
       (do
         (set! (-> $scope :name) (-> $scope :newName))
         (set! (-> $scope :newName) "")))))

  (set!
   (-> $scope :sendMessage)
   (fn []
     (. socket (emit {:type "text"
                      :message (-> $scope :message)}))
     (. (-> $scope :messages)
        (push {:text (-> $scope :message), :user (-> $scope :name)}))
     (set! (-> $scope :message) "")))
)
