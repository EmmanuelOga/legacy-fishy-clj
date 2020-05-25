(ns rainbowfish.cli
  (:import java.nio.file.Paths))

(def ^:dynamic options
  "Main program options"
  {:root "c:/Users/emman/workspace/projects/eogadev"})

(defn relpath
  "Return a path relative to the root path of the project files"
  [& paths]
  (->
   (Paths/get (:root options) (into-array String paths))
   (.toAbsolutePath)
   (.toString)
   (.replace "\\" "/")))

