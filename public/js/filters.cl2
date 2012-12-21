(..
 angular
 (module "myApp.filters" [])
 (filter
  "interpolate"
  ["version"
   (fn
    [version]
    (fn
     [text]
     (.. (String text) (replace #"/\%VERSION\%/gm" version))))]))
