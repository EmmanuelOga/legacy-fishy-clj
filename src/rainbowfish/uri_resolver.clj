(ns rainbowfish.uri-resolver
  "Extends saxon's standard URI resolver to open BaseX documents when an
  URI has the `basex://` scheme."
  (:gen-class
   :name rainbowfish.UriResolver
   :extends net.sf.saxon.lib.StandardURIResolver
   :exposes-methods {resolve resolveSuper})
  (:require [clojure.string :as str]
            [rainbowfish.xmldb :as xmldb])
  (:import java.io.ByteArrayInputStream
           javax.xml.transform.sax.SAXSource
           javax.xml.transform.Source
           org.xml.sax.InputSource))

(defn resolve-basex
  "Attempt to open a document from BaseX. href should contain a url like
  `basex://db-name/db-path`. `base` will typically be the .xq file
  where the document is being opened, and is ignored on this
  method (url should always include db name and be absolute)."
  [href base]
  (let [uri (java.net.URI. href)
        ;; Peform a BaseX query to retrieve the doc.  Unfortunatelly
        ;; this opens an additional network request but should be ok
        ;; for now.
        src (xmldb/query
             "declare variable $db external;
              declare variable $path external;
              db:open($db, $path)"
             [["$db" (.getHost uri)]
              ["$path" (.getPath uri)]])
        bais (ByteArrayInputStream. (.getBytes src "UTF-8"))
        is (InputSource. bais)]
    (doto (SAXSource. is)
      (.setSystemId href))))

(defn ^Source -resolve
  "Override `resolve` to handle `basex://` URIs"
  [this ^String href ^String base]
  (if (str/starts-with? href "basex://")
    (resolve-basex href base)
    (.resolveSuper this href base)))
