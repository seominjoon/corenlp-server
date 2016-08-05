import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.scoref.DocumentProcessor;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.*;
import com.google.gson.Gson;

public class Server {

    public static void main(String[] args) throws Exception {
        int port = 8000;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);
        System.out.println("Port: " + port);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/doc", new DocumentHandler());
        server.createContext("/sent", new SentenceHandler());
        server.createContext("/dep", new DependencyHandler());
        server.createContext("/const", new ConstituencyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private static class DocumentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String in = get(t);
            Document doc = new Document(in);
            List<String> sentenceStrings = new ArrayList<>();
            for (Sentence sentence : doc.sentences()) {
                sentenceStrings.add(sentence.toString());
            }
            String out = new Gson().toJson(sentenceStrings);
            send(t, out);
        }
    }

    private static class SentenceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String in = get(t);
            Sentence sent = new Sentence(in);
            List<String> words = new ArrayList<>();
            for (String word : sent.words()) {
                words.add(word);
            }
            String out = new Gson().toJson(words);
            send(t, out);
        }
    }

    private static class ConstituencyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String in = get(t);
            Sentence sent = new Sentence(in);
            String out = sent.parse().toString();
            send(t, out);
        }
    }

    /*
    each tuple is (dw, d, i, gw, g, j, l):
    d: dependent
     */
    private static class DependencyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String in = get(t);
            String outString = "";
            try {
                Sentence sent = new Sentence(in);
                List<List<Object>> deps = new ArrayList<>();
                IndexedWord[] indexedWords = new IndexedWord[sent.length()];
                for (SemanticGraphEdge e : sent.dependencyGraph().edgeListSorted()) {
                    List<Object> list = new ArrayList<>();
                    int di = e.getDependent().index() - 1;
                    int gi = e.getGovernor().index() - 1;
                    list.add(di);
                    list.add(gi);
                    list.add(e.getRelation().getShortName());
                    deps.add(list);
                    System.out.println(e);
                    indexedWords[di] = e.getDependent();
                    indexedWords[gi] = e.getGovernor();
                }

                List<Object> lists = new ArrayList<>();
                for (IndexedWord w : indexedWords) {
                    lists.add(indexedWordToList(w));
                }
                List<Object> out = new ArrayList<>();
                out.add(lists);
                out.add(deps);
                outString = new Gson().toJson(out);
            } catch (Exception e){
                System.out.println(e.getMessage());
                outString = "error";
            }
            send(t, outString);

        }
    }

    public static List<Object> indexedWordToList(IndexedWord w) {
        List<Object> out = new ArrayList<>();
        out.add(w.word());
        out.add(w.tag());
        out.add(w.beginPosition());
        out.add(w.endPosition());
        return out;
    }

    private static String get(HttpExchange t) {
        String out = convertStreamToString(t.getRequestBody());
        System.out.println("Received: " + out);
        return out;
    }

    private static void send(HttpExchange t, String response) throws IOException {
        byte[] byteResponse = response.getBytes("UTF-8");
        t.sendResponseHeaders(200, byteResponse.length);
        OutputStream os = t.getResponseBody();
        os.write(byteResponse);
        os.close();
        System.out.println("Sending: " + response);
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
