(defn AppCtrl [$scope socket]
  (do
    (..
     socket
     (on
      "init"
      (fn
        [data]
        (set! (-> $scope :name) (-> data :name))
        (set! (-> $scope :users) (-> data :users)))))
    (..
     socket
     (on
      "send-message"
      (fn [message]
        (.. (-> $scope :messages) (push message)))))
    (..
     socket
     (on
      "change-name"
      (fn [data]
        (changeName (-> data :oldName) (-> data :newName)))))
    (..
     socket
     (on
      "new-user"
      (fn [data]
        (..
         (-> $scope :messages)
         (push
          {:text (+ "User " (-> data :name) " has joined."),
           :user "chatroom"}))
        (.. (-> $scope :users) (push (-> data :name))))))
    (..
     socket
     (on
      "user-left"
      (fn [data]
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
             (do (.. (-> $scope :users) (splice i 1)) break)))))))
    (def
      changeName
      (fn [oldName newName]
        (def i)
        (dofor
         [(set! i 0) (< i (-> $scope :users :length)) (inc-after! i)]
         (if
             (=== (get (-> $scope :users) i) oldName)
           (set! (get (-> $scope :users) i) newName)))
        (..
         (-> $scope :messages)
         (push
          {:text (+ "User " oldName " is now known as " newName "."),
           :user "chatroom"}))
        ))
    (set!
     (-> $scope :changeName)
     (fn []
       (..
        socket
        (emit "change-name"
              {:name (-> $scope :newName)}
              (fn [result]
                (if (not result)
                  (alert "There was an error changing your name")
                  (do
                    (changeName (-> $scope :name) (-> $scope :newName))
                    (set! (-> $scope :name) (-> $scope :newName))
                    (set! (-> $scope :newName) ""))))))))
    (set! (-> $scope :messages) [])
    (set!
     (-> $scope :sendMessage)
     (fn []
       (. socket (emit {:type "text"
                        :message (-> $scope :message)}))
       (. (-> $scope :messages)
          (push {:text (-> $scope :message), :user (-> $scope :name)}))
       (set! (-> $scope :message) "")))))
