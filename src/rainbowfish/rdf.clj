(ns rainbowfish.rdf)

(def uri
  "List of commonly used uri prefixes"
  {:dc "http://purl.org/dc/terms/"
   :foaf "http://xmlns.com/foaf/0.1/"
   :skos "http://www.w3.org/2004/02/skos/core#"
   :xsd "http://www.w3.org/2001/XMLSchema#"

   :array "http://www.w3.org/2005/xpath-functions/array"
   :err "http://www.w3.org/2005/xqt-errors"
   :fn "http://www.w3.org/2005/xpath-functions"
   :map "http://www.w3.org/2005/xpath-functions/map"
   :saxon "http://saxon.sf.net/"
   :xsl "http://www.w3.org/1999/XSL/Transform"

   ; From the validating RDF book.
   (keyword ":") "http://example.org/"
   :ex "http://example.org/"
   :cdt "http://example.org/customDataTypes#"
   :cex "http://purl.org/weso/computex/ontology#"
   :dbr "http://dbpedia.org/resource/"
   :org "http://www.w3.org/ns/org#"
   :owl "http://www.w3.org/2002/07/owl#"
   :qb "http://purl.org/linked-data/cube#"
   :rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   :rdfs "http://www.w3.org/2000/01/rdf-schema#"
   :schema "http://schema.org/"
   :sh "http://www.w3.org/ns/shacl#"
   :sx "http://shex.io/ns/shex#"

   :eo "https://emmanueloga.com/"
   :rp "https://eoga.dev/rdfpub/"
   :sd "https://eoga.dev/sdoc/"})
