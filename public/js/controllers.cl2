(defn sock-handler
  [data $scope]
  (console.log "Got this msg: " data)
  (case (:type data)
    "init"
    (do
      (console.log "initializing... Go!")
      (set! (-> $scope :name) (-> data :name))
      (set! (-> $scope :users) (-> data :users)))

    "text"
    (.. (-> $scope :messages)
        (push {:text (-> data :message), :user (-> data :name)}))


    "change-name"
    (change-name (-> data :old-name) (-> data :new-name)
                 $scope)

    "new-user"
    (do
      (..
       (-> $scope :messages)
       (push
        {:text (+ "User " (-> data :name) " has joined."),
         :user "chatroom"}))
      (.. (-> $scope :users) (push (-> data :name))))

    "user-left"
    (do
      (..
       (-> $scope :messages)
       (push
        {:text (+ "User " (-> data :name) " has left."),
         :user "chatroom"}))
      (set! (:users $scope)
            (filter #(not= (:name data))
                    (:users $scope))))
    ))

(defn change-name
  [old-name new-name $scope]
  (console.log "Before, users: " (-> $scope :users))
  (console.log "and old name is: " old-name)
  (set! (-> $scope :users)
        (conj (filter #(not= % old-name) (-> $scope :users))
              new-name))
  (console.log "After, users: " (-> $scope :users))
  (if (= old-name (:name $scope))
    (set! (-> $scope :name) new-name))
  (..
   (-> $scope :messages)
   (push
    {:text (+ "User " old-name " is now known as " new-name "."),
     :user "chatroom"})))

(defn AppCtrl [$scope socket]
  (set! (-> $scope :messages) [])
  (defn $scope.changeName []
    (if (not (.. socket (emit {:type "change-name"
                               :name (-> $scope :newName)})))
      (alert "There was an error changing your name")
      (set! (-> $scope :newName) "")))
  (defn $scope.sendMessage []
    (. socket (emit {:type "text"
                     :message (-> $scope :message)}))
    (. (-> $scope :messages)
       (push {:text (-> $scope :message), :user (-> $scope :name)}))
    (set! (-> $scope :message) "")))
