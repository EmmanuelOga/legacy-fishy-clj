(ns rainbowfish.client)

(defn init
  []
  (.log js/console "Ready!!!"))

(defn reload
  []
  (.log js/console "Reloaded."))
