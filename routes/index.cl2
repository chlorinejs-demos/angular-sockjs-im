(import! [:private "boot.cl2"])

(set!
 (-> exports :index)
 (fn [req res] (.. res (render "index"))))

(set!
 (-> exports :partials)
 (fn
  [req res]
  (def name (-> req :params :name))
  (.. res (render (+ "partials/" name)))))
