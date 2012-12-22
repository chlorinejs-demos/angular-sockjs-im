(..
 app
 (factory
  "socket"
  (fn [$rootScope]
    (def sock (new SockJS "http://localhost:3000/chat"))
    {:emit
     (fn [data callback]
       (def args arguments)
       (..
        $rootScope
        ($apply
         (fn [] (if callback (.. callback (apply socket args))))))
       (.. sock (send (serialize data))))
     :on
     (fn [eventName callback]
       (..
        sock
        (on
         eventName
         (fn []
           (def args arguments)
           (..
            $rootScope
            ($apply
             (fn [] (.. callback (apply socket args)))))))))})))
