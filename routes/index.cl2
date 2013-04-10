(defn exports.index
  [req res]
  (.. res (render "index")))

(defn exports.partials
  [req res]
  (def name (-> req :params :name))
  (.. res (render (+ "partials/" name))))
