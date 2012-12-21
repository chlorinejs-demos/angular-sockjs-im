(defn
 AppCtrl
 [$scope socket]
 (do
  (..
   socket
   (on
    "init"
    (fn
     [data]
     (do
      (set! (-> $scope :name) (-> data :name))
      (set! (-> $scope :users) (-> data :users))
      undefined))))
  (..
   socket
   (on
    "send:message"
    (fn
     [message]
     (do (.. (-> $scope :messages) (push message)) undefined))))
  (..
   socket
   (on
    "change:name"
    (fn
     [data]
     (do
      (changeName (-> data :oldName) (-> data :newName))
      undefined))))
  (..
   socket
   (on
    "user:join"
    (fn
     [data]
     (do
      (..
       (-> $scope :messages)
       (push
        {:text (+ "User " (-> data :name) " has joined."),
         :user "chatroom"}))
      (.. (-> $scope :users) (push (-> data :name)))
      undefined))))
  (..
   socket
   (on
    "user:left"
    (fn
     [data]
     (do
      (..
       (-> $scope :messages)
       (push
        {:text (+ "User " (-> data :name) " has left."),
         :user "chatroom"}))
      (do (def i nil) (def user nil))
      (dofor
       [(set! i 0) (< i (-> $scope :users :length)) (inc-after! i)]
       (do
        (set! user (get (-> $scope :users) i))
        (if
         (=== user (-> data :name))
         (do (.. (-> $scope :users) (splice i 1)) break))))
      undefined))))
  (def
   changeName
   (fn
    [oldName newName]
    (do
     (def i nil)
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
     undefined)))
  (set!
   (-> $scope :changeName)
   (fn
    []
    (do
     (..
      socket
      (emit
       "change:name"
       {:name (-> $scope :newName)}
       (fn
        [result]
        (do
         (if
          (not result)
          (alert "There was an error changing your name")
          (do
           (changeName (-> $scope :name) (-> $scope :newName))
           (set! (-> $scope :name) (-> $scope :newName))
           (set! (-> $scope :newName) "")))
         undefined))))
     undefined)))
  (set! (-> $scope :messages) [])
  (set!
   (-> $scope :sendMessage)
   (fn
    []
    (do
     (.. socket (emit "send:message" {:message (-> $scope :message)}))
     (..
      (-> $scope :messages)
      (push {:text (-> $scope :message), :user (-> $scope :name)}))
     (set! (-> $scope :message) "")
     undefined)))
  undefined))
