(def sockjs-url (+* window.location.protocol "//"
                    window.location.host
                    "/chat"))
(..
 app
 (factory
  "socket"
  (fn [$rootScope]
    (def sock (new SockJS sockjs-url undefined
                   {:protocols_whitelist
                    ['xdr-streaming 'xhr-streaming
                     'iframe-eventsource 'iframe-htmlfile
                     'xdr-polling 'xhr-polling
                     'iframe-xhr-polling 'jsonp-polling]}))
    (defn sock.onopen []
      (console.log "Connected"))
    (defn sock.emit[data]
      (. sock send (serialize data)))
    (defn sock.onmessage [message]
      ($rootScope.$apply
       (fn [] (sock-handler (deserialize (:data message)) $rootScope))))
    sock)))
