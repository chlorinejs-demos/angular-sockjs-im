(defn AppCtrl [$scope socket]
  (set! (-> $scope :messages) [])
  ;;(def socket (new SockJS "http://localhost:3000/chat"))
  (set! (:onopen socket)
        (fn []
          (console.log "Connected")))
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
            (do (console.log "oh this shit run!")
                (change-name (-> data :old-name) (-> data :new-name))
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
              (def i)
              (def user)
              (dofor
               [(set! i 0) (< i (-> $scope :users :length)) (inc-after! i)]
               (do
                 (set! user (get (-> $scope :users) i))
                 (if
                     (=== user (-> data :name))
                   (do (.. (-> $scope :users) (splice i 1)) break))))
              break)
            )))

  (defn
    change-name
    [old-name new-name]
    (console.log "I got called")
    (def i)
    (dofor
     [(set! i 0) (< i (-> $scope :users :length)) (inc-after! i)]
     (if
         (=== (get (-> $scope :users) i) old-name)
       (set! (get (-> $scope :users) i) new-name)))
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
         (change-name (-> $scope :name) (-> $scope :newName))
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
  ;; ready. Now init

  )
