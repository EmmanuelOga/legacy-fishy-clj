(ns rainbowfish.file-util
  (:import java.nio.file.Paths))

(defn relpath
  "Returns a path relative to the given root path."
  [root & paths]
  (->
   (Paths/get root (into-array String paths))
   (.toAbsolutePath)
   (.normalize)
   (.toString)
   (.replace "\\" "/")))

