(..
 app
 (factory
  "socket"
  (fn
   [$rootScope]
   (do
    (def socket (.. io connect))
    {:emit
     (fn
      [eventName data callback]
      (do
       (..
        socket
        (emit
         eventName
         data
         (fn
          []
          (do
           (def args arguments)
           (..
            $rootScope
            ($apply
             (fn
              []
              (do
               (if callback (.. callback (apply socket args)))
               undefined))))
           undefined))))
       undefined)),
     :on
     (fn
      [eventName callback]
      (do
       (..
        socket
        (on
         eventName
         (fn
          []
          (do
           (def args arguments)
           (..
            $rootScope
            ($apply
             (fn [] (do (.. callback (apply socket args)) undefined))))
           undefined))))
       undefined))}))))
