package rainbowfish;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;

public class JenaTest {
    public static void main() {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
            .destination("http://localhost:3330/ds");
        
        Query query = QueryFactory.create("SELECT * { BIND('Hello'as ?text) }");

        // In this variation, a connection is built each time. 
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) { 
            conn.queryResultSet(query, ResultSetFormatter::out);
        }
    }
}


