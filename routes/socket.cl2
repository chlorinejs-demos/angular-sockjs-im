(import! [:private "boot.cl2"])

(def
 userNames
 ((fn
   []
   (do
    (def names {})
    (def
     claim
     (fn [name]
      (if (or (not name) (get names name))
        false
        (do (set! (get names name) true) true))))
    (def
     getGuestName
     (fn
      []
      (do
       (def name nil)
       (def nextUserId 1)
       (do-while (not (claim name))
        (do
         (set! name (+* "Guest " nextUserId))
         (inc! nextUserId)))
       name)))
    (def
     get-user
     (fn
      []
      (do
       (def res [])
       (dokeys [user names] (.. res (push user)))
       res)))
    (def
     free
     (fn
      [name]
      (if (get names name) (delete (get names name)))))
    {:getGuestName getGuestName, :get get-user, :free free, :claim claim}))))

(set!
 (-> module :exports)
 (fn
  [socket]
  (def name (.. userNames getGuestName))
  (.. socket (emit "init" {:users (.. userNames get), :name name}))
  (.. (-> socket :broadcast) (emit "user:join" {:name name}))
  (..
   socket
   (on
    "send:message"
    (fn
      [data]
      (..
       (-> socket :broadcast)
       (emit "send:message" {:text (-> data :message), :user name})))))
  (..
   socket
   (on
    "change:name"
    (fn
      [data func]
      (if
          (.. userNames (claim (-> data :name)))
        (do
          (def oldName name)
          (.. userNames (free oldName))
          (set! name (-> data :name))
          (..
           (-> socket :broadcast)
           (emit "change:name" {:newName name, :oldName oldName}))
          (func true))
        (func false)))))
  (..
   socket
   (on
    "disconnect"
    (fn
      []
      (.. (-> socket :broadcast) (emit "user:left" {:name name}))
      (.. userNames (free name)))))))
