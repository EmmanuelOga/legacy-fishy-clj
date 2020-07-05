(ns rainbowfish.xslt-factory
  (:gen-class
   :name rainbowfish.XsltFactory
   :extends net.sf.saxon.jaxp.SaxonTransformerFactory
   :post-init configure)
  (:import net.sf.saxon.lib.Feature))

(def extension-functions
  "Collection of extension functions to register when creating an
  instance of Saxon Processor"
  (atom []))

(defn -configure
  "Generated Java class will call this method right after
  construction. Here we get a chance to grab Saxon's processor for
  configuration purposes (for example, adding extension functions)."
  [this & args]
  (let [processor (.getProcessor this)]
    ; Configure URI Resolver.
    (.setConfigurationProperty
     processor
     Feature/URI_RESOLVER_CLASS
     "rainbowfish.UriResolver")

    ; TODO: add extension functions.
    ))
