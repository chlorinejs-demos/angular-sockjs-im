(..
 angular
 (module "myApp.directives" [])
 (directive
  "appVersion"
  ["version"
   (fn
    [version]
    (fn [scope elm attrs] (do (.. elm (text version)) undefined)))]))
