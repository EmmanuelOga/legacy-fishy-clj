package rainbowfish;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.atlas.web.HttpException;

public class JenaClient {
    public static void get() {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
            .destination("http://localhost:3330/ds");
        
        Query query = QueryFactory.create("SELECT ?s ?p ?o { ?s ?p ?o . }");

        // In this variation, a connection is built each time. 
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) { 
            conn.queryResultSet(query, ResultSetFormatter::out);
        }
    }

    
    public static Model fetch(String namedGraph) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
            .destination("http://localhost:3330/ds");
        
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) { 
            return conn.fetch(namedGraph);
        } catch (org.apache.jena.atlas.web.HttpException e) {
            System.out.println(e);
            return null;
        }
    }

    public static void put(String namedGraph, Model model) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
            .destination("http://localhost:3330/ds");

        
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) { 
            conn.put(namedGraph, model);
        } catch (org.apache.jena.atlas.web.HttpException e) {
            System.out.println(e);
        }
    }
}

