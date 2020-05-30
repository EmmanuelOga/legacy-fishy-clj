(ns rainbowfish.client)

(defn init
  []
  (.log js/console "Ready!!!"))

(defn ^:dev/after-load start []
  (js/console.log "start"))

(defn ^:dev/before-load stop []
  (js/console.log "stop"))

