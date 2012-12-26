(..
 app
 (factory
  "socket"
  (fn [$rootScope]
    (def sock (new SockJS "http://localhost:3000/chat"))
    (defn sock.onopen []
      (console.log "Connected"))
    (defn sock.emit[data]
      (. sock send (serialize data)))
    (defn sock.onmessage [message]
      ($rootScope.$apply
       (fn [] (sock-handler (deserialize (:data message)) $rootScope))))
    sock)))
