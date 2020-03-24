package org.peopletry.siqi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Hello world!
 *
 */

public class IndexFiles {

    private static Object BM25Similarity;

    private IndexFiles() {}

    /** Index all text files under a directory. */
    public static void main(String[] args) {
        String usage = "java org.apache.lucene.demo.IndexFiles"
                + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                + "in INDEX_PATH that can be searched with SearchFiles";
        String indexPath = "index";
        String docsPath = null;
        boolean create = true;
        for(int i=0;i<args.length;i++) {
            if ("-index".equals(args[i])) {
                indexPath = args[i+1];
                i++;
            } else if ("-docs".equals(args[i])) {
                docsPath = args[i+1];
                i++;
            } else if ("-update".equals(args[i])) {
                create = false;
            }
        }

        if (docsPath == null) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        //docsPath = "/Users/siqiwei/Desktop/Assignment_Two/ft/";
        final Path docDir = Paths.get(docsPath);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        System.out.println("Index path ='" + indexPath);
        System.out.println("doc path =" + docsPath);
        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new EnglishAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            System.out.println("analyzer"+ analyzer);
            iwc.setSimilarity(new BooleanSimilarity());
            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:

                iwc.setOpenMode(OpenMode.CREATE);
            } else {
                // Add new documents to an existing index:
                iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }

            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer.  But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //
            // iwc.setRAMBufferSizeMB(256.0);
            System.out.println("here iwc = " + iwc);
            System.out.println("Codec is "+Codec.availableCodecs());
            IndexWriter writer = new IndexWriter(dir, iwc);
            System.out.println("here writer = "+ writer + "iwc = " + iwc );

            indexDocs(writer, docDir);

            // NOTE: if you want to maximize search performance,
            // you can optionally call forceMerge here.  This can be
            // a terribly costly operation, so generally it's only
            // worth it when your index is relatively static (ie
            // you're done adding documents to it):
            //
            // writer.forceMerge(1);

            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }


    static void indexDocs(final IndexWriter writer, Path path) throws IOException {

        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    try {
                        if (file.equals("/Users/siqiwei/Desktop/Assignment_Two/fbis/.DS_Store")){
                            System.out.println("DS_store here");
                        }
                        else {
                            indexDoc(writer, file);
                        }

                    } catch (IOException ignore) {
                        System.out.println("can't index file");
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDoc(writer, path);
        }
    }
    // ft
    // /Users/siqiwei/Desktop/Assignment_Two/ft/ft911/
    /** Indexes a single document */
    static void indexDoc(IndexWriter writer, Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            System.out.println("file = " + file);

            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            int mark = -1;
            int count = 0;
            ArrayList<Document> doc = new ArrayList<Document>();
            Document doc_0 = new Document();
            doc.add(doc_0);
            while ((line = br.readLine()) != null) {
                System.out.println("index go here now, line is " + line);


                if (line.startsWith("<DOC>")) {
                    //需要push document
                    System.out.println("line is " + line);
                    Document newdoc = new Document();
                    doc.add(newdoc);
                    if (count != 0) {
                        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                            // New index, so we just add the document (no old document can be there):
                            System.out.println("adding " + file);
                            writer.addDocument(doc.get(count - 1));
                        } else {
                            System.out.println("updating " + file);
                            writer.updateDocument(new Term("path", file.toString()), doc.get(count - 1));
                        }
                        //writer.addDocument(doc.get(count-1));
                    }
                    count = count + 1;
                } else if (line.startsWith("<PUB>")) {
                    String pub = line.substring(5);
                    doc.get(count - 1).add(new TextField("PUB", pub, Field.Store.YES));
                } else if (line.startsWith("</PUB>")) {
                    mark = -1;
                } else if (line.startsWith("</DOC>")) {

                    mark = -1;
                } else if (line.startsWith("<PAGE>")) {
                    mark = 3;
                } //<PAGE>
                else if (line.startsWith("<TEXT>")) {
                    mark = 2;
                } // <TEXT>
                else if (line.startsWith("<DATE>")) {
                    String date = line.substring(6);
                    doc.get(count - 1).add(new TextField("DATE", date, Field.Store.YES));
                    System.out.println("date is " + date);

                } else if (line.startsWith("</PAGE>")) {
                    mark = -3;
                } else if (line.startsWith("<DOCNO>")) {

                    String docno = StringUtils.substringBetween(line, "<DOCNO>", "</DOCNO>");
                    System.out.println("docno is " + docno);
                    doc.get(count - 1).add(new TextField("DOCNO", docno, Field.Store.YES));
                } //<DOCNO>

                else if (line.startsWith("</DATE>")) {
                    System.out.println("date end here");
                } else if (line.startsWith("</TEXT>")) {
                    mark = -2;
                } else if (line.startsWith("<BYLINE>")) {
                    mark = 4;
                } else if (line.startsWith("</BYLINE>")) {
                    mark = -4;
                } else if (line.startsWith("<PROFILE>")) {
                    // substract profile name
                    String profile = StringUtils.substringBetween(line, "<PROFILE>", "</PROFILE>");
                    System.out.println("profile is " + profile);
                    doc.get(count - 1).add(new TextField("PROFILE", profile, Field.Store.YES));

                } // <PROFILE>
                else if (line.startsWith("<HEADLINE>")) {
                    mark = 1;
                } // <HEADLINE>
                else if (line.startsWith("<DATELINE>")) {
                    mark = 5;
                } // <DATELINE>
                else if (line.startsWith("</HEADLINE>")) {
                    mark = -1;
                } else if (line.startsWith("</DATELINE>")) {
                    mark = -1;
                } else if (mark == 1) {
                    doc.get(count - 1).add(new TextField("HEADLINE", line, Field.Store.YES));
                } else if (mark == 2) {
                    doc.get(count - 1).add(new TextField("TEXT", line, Field.Store.YES));
                } else if (mark == 3) {
                    doc.get(count - 1).add(new TextField("PAGE", line, Field.Store.YES));
                } else if (mark == 4) {
                    doc.get(count - 1).add(new TextField("BYLINE", line, Field.Store.YES));
                } else if (mark == 5) {
                    doc.get(count - 1).add(new TextField("DATELINE", line, Field.Store.YES));
                }


                //System.out.println("mark is " + mark);


            }
            writer.addDocument(doc.get(count - 1));
            System.out.println(doc.size());

        }
    }

       /** Indexes a single document
        // fr94 需要先remove  comment line
        //  /siqiwei/Desktop/Assignment_Two/fr94/01/FR940104-0.nocomments
        static void indexDoc(IndexWriter writer, Path file) throws IOException {
            try (InputStream stream = Files.newInputStream(file)) {
                System.out.println("file = " + file);
                // make a new, empty document
                //Document doc = new Document();

                // Add the path of the file as a field named "path".  Use a
                // field that is indexed (i.e. searchable), but don't tokenize
                // the field into separate words and don't index term frequency
                // or positional information:
                //Field pathField = new StringField("path", file.toString(), Field.Store.YES);
                //doc.add(pathField);

                //doc.add(new LongPoint("modified", lastModified));

                // Add the contents of the file to a field named "contents".  Specify a Reader,
                // so that the text of the file is tokenized and indexed, but not stored.
                // Note that FileReader expects the file to be in UTF-8 encoding.
                // If that's not the case searching for special characters will fail.
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String line = null;
                int curr = -100;
                int count = 0;
                ArrayList<Document> doc = new ArrayList<Document>();
                Document doc_0 = new Document();
                doc.add(doc_0);
                Stack<Integer> stack = new Stack<Integer>();

                while ((line = br.readLine()) != null) {
                    System.out.println("index go here now, line is "+line);
                    if (line.startsWith("<DOC>")){
                        //需要push document
                        System.out.println("line is "+line);
                        Document newdoc = new Document();
                        doc.add(newdoc);
                        if (count != 0){
                            if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                                // New index, so we just add the document (no old document can be there):
                                System.out.println("adding " + file);
                                writer.addDocument(doc.get(count-1));
                            } else {
                                System.out.println("updating " + file);
                                writer.updateDocument(new Term("path", file.toString()), doc.get(count-1));
                            }
                            //writer.addDocument(doc.get(count-1));
                        }
                        count = count + 1;
                        curr = 1;
                        stack.push(curr);
                    }

                    else if (line.startsWith("</DOC>")){
                        stack.pop();
                        curr = -100;
                    }
                    else if (line.startsWith("<TEXT>")){
                        curr = 2;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</TEXT>")){
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<PARENT>")){
                        String parent = StringUtils.substringBetween(line, "<PARENT>", "</PARENT>");
                        doc.get(count-1).add(new TextField("PARENT", parent, Field.Store.YES));
                        System.out.println("parent is "+parent);
                    }
                    else if (line.startsWith("<DOCNO>")){
                        String docno = StringUtils.substringBetween(line, "<DOCNO>", "</DOCNO>");
                        System.out.println("docno is "+docno);
                        doc.get(count-1).add(new TextField("DOCNO", docno, Field.Store.YES));
                    } //<DOCNO>

                    else if (line.startsWith("<USDEPT>")){
                        String usdept = StringUtils.substringBetween(line, "<USDEPT>", "</USDEPT>");
                        doc.get(count-1).add(new TextField("USDEPT", usdept, Field.Store.YES));
                    }
                    else if (line.startsWith("<USBUREAU>")){
                        String usbureau = StringUtils.substringBetween(line, "<USBUREAU>", "</USBUREAU>");
                        doc.get(count-1).add(new TextField("USBUREAU", usbureau, Field.Store.YES));
                    }
                    else if (line.startsWith("<CFRNO>")){
                        String cfrno = StringUtils.substringBetween(line, "<CFRNO>", "</CFRNO>");
                        doc.get(count-1).add(new TextField("CFRNO", cfrno, Field.Store.YES));
                    }
                    else if (line.startsWith("<RINDOCK>")){
                        String rindock = StringUtils.substringBetween(line, "<RINDOCK>", "</RINDOCK>");
                        doc.get(count-1).add(new TextField("RINDOCK", rindock, Field.Store.YES));
                    }
                    else if (line.startsWith("<FOOTCITE>")) {
                        String footcite = StringUtils.substringBetween(line, "<FOOTCITE>", "</FOOTCITE>");
                        doc.get(count-1).add(new TextField("FOOTCITE", footcite, Field.Store.YES));
                    }
                    else if (line.startsWith("<DOCTITLE>")){
                        String doctitle = StringUtils.substringBetween(line, "<DOCTITLE>", "</DOCTITLE>");
                        doc.get(count-1).add(new TextField("DOCTITLE", doctitle, Field.Store.YES));
                    }
                    else if (line.startsWith("<FOOTNAME>")) {
                        curr = 3;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</FOOTNAME>")) {
                        stack.pop();
                        curr = stack.peek();
                    }

                    else if (line.startsWith("<AGENCY>")) {
                        curr = 4;
                        stack.push(curr);
                    }

                    else if (line.startsWith("</AGENCY>")){
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<ACTION>")) {
                        curr = 5;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</ACTION>")){
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<SUMMARY>")) {
                        curr = 6;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</SUMMARY>")){
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<DATE>")) {
                        curr = 7;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</DATE>")){
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<FURTHER>")) {
                        curr = 8;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</FURTHER>")){
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<SUPPLEM>")) {
                        curr = 9;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</SUPPLEM>")){
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<SIGNER>")) {
                        curr = 10;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</SIGNER>")){
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<SIGNJOB>")) {
                        curr = 11;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</SIGNJOB>")){
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<FRFILING>")) {
                        curr = 12;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</FRFILING>")){
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<BILLING>")) {
                        curr = 13;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</BILLING>")){
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<FOOTNOTE>")) {
                        curr = 14;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</FOOTNOTE>")) {
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<ADDRESS>")) {
                        curr = 15;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</ADDRESS>")) {
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<FOOTNOTE>")) {
                        curr = 16;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</FOOTNOTE>")) {
                        stack.pop();
                        curr = stack.peek();
                    }
                    else if (line.startsWith("<TABLE>")) {
                        curr = 16;
                        stack.push(curr);
                    }
                    else if (line.startsWith("</TABLE>")) {
                        stack.pop();
                        curr = stack.peek();
                    }


                    else if (curr == 7) { doc.get(count-1).add(new TextField("DATE", line, Field.Store.YES)); }
                    else if (curr == 8) { doc.get(count-1).add(new TextField("FURTHER", line, Field.Store.YES)); }
                    else if (curr == 9) { doc.get(count-1).add(new TextField("SUPPLEM", line, Field.Store.YES)); }
                    else if (curr == 10) { doc.get(count-1).add(new TextField("SIGNER", line, Field.Store.YES)); }
                    else if (curr == 11) { doc.get(count-1).add(new TextField("SIGNJOB", line, Field.Store.YES)); }
                    else if (curr == 12) { doc.get(count-1).add(new TextField("FRFILING", line, Field.Store.YES)); }
                    else if (curr == 13) { doc.get(count-1).add(new TextField("BILLING", line, Field.Store.YES)); }
                    else if (curr == 14) { doc.get(count-1).add(new TextField("FOOTNOTE", line, Field.Store.YES)); }
                    else if (curr == 15) { doc.get(count-1).add(new TextField("ADDRESS", line, Field.Store.YES)); }
                    //else if (mark == 1) { doc.get(count-1).add(new TextField("HEADLINE", line, Field.Store.YES)); }
                    else if (curr == 16) { doc.get(count-1).add(new TextField("FOOTNOTE", line, Field.Store.YES)); }
                    else if (curr == 2){ doc.get(count-1).add(new TextField("TEXT", line, Field.Store.YES)); }
                    else if (curr == 3){ doc.get(count-1).add(new TextField("FOOTCITE", line, Field.Store.YES));}
                    else if (curr == 4){ doc.get(count-1).add(new TextField("AGENCY", line, Field.Store.YES));}
                    else if (curr == 5) { doc.get(count-1).add(new TextField("ACTION", line, Field.Store.YES)); }
                    else if (curr == 6) { doc.get(count-1).add(new TextField("SUMMARY", line, Field.Store.YES)); }
                }

                writer.addDocument(doc.get(count-1));
                System.out.println(doc.size());
            }
            */

    /** Indexes a single document
    // -docs  /Users/siqiwei/Desktop/Assignment_Two/fbis/fb396001

    // fbis
    static void indexDoc(IndexWriter writer, Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            System.out.println("file = " + file);
            if (!file.equals("/Users/siqiwei/Desktop/Assignment_Two/fbis/.DS_Store")) {

                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                String line = null;
                int curr = -100;
                int count = 0;
                ArrayList<Document> doc = new ArrayList<Document>();
                Document doc_0 = new Document();
                doc.add(doc_0);
                Stack<Integer> stack = new Stack<Integer>();

                while ((line = br.readLine()) != null) {
                    System.out.println("index go here now, line is " + line);
                    if (line.startsWith("<DOC>")) {
                        //需要push document
                        System.out.println("line is " + line);
                        Document newdoc = new Document();
                        doc.add(newdoc);
                        if (count != 0) {
                            if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                                // New index, so we just add the document (no old document can be there):
                                System.out.println("adding " + file);
                                writer.addDocument(doc.get(count - 1));
                            } else {
                                System.out.println("updating " + file);
                                writer.updateDocument(new Term("path", file.toString()), doc.get(count - 1));
                            }
                            //writer.addDocument(doc.get(count-1));
                        }
                        count = count + 1;
                        curr = 1;
                        stack.push(curr);
                    } else if (line.startsWith("</DOC>")) {
                        stack.pop();
                        curr = -100;
                    } else if (line.startsWith("<TEXT>")) {
                        curr = 2;
                        stack.push(curr);
                    } else if (line.startsWith("</TEXT>")) {
                        stack.pop();
                        curr = stack.peek();
                    } else if (line.startsWith("<HT>")) {
                        String ht = StringUtils.substringBetween(line, "<HT>", "</HT>");
                        doc.get(count - 1).add(new TextField("HT", ht, Field.Store.YES));
                        System.out.println("ht is " + ht);
                    } else if (line.startsWith("<AU>")) {
                        String au = StringUtils.substringBetween(line, "<AU>", "</AU>");
                        doc.get(count - 1).add(new TextField("AU", au, Field.Store.YES));
                        System.out.println("au is " + au);
                    } else if (line.startsWith("<DOCNO>")) {
                        String docno = StringUtils.substringBetween(line, "<DOCNO>", "</DOCNO>");
                        System.out.println("docno is " + docno);
                        doc.get(count - 1).add(new TextField("DOCNO", docno, Field.Store.YES));
                    } //<DOCNO>
                    else if (line.startsWith("<DATE1>")) {
                        String date1 = StringUtils.substringBetween(line, "<DATE1>", "</DATE1>");
                        System.out.println("date1 is " + date1);
                        doc.get(count - 1).add(new TextField("DATE1", date1, Field.Store.YES));
                    } else if (line.startsWith("<H1>")) {
                        String H1 = null;
                        if (line.contains("<TI>")) {
                            H1 = StringUtils.substringBetween(line, "<TI>", "</TI>");
                        } else {
                            H1 = StringUtils.substringBetween(line, "<H1>", "</H1>");
                        }
                        System.out.println("H1 is " + H1);
                        doc.get(count - 1).add(new TextField("H1", H1, Field.Store.YES));
                    } else if (line.startsWith("<H2>")) {
                        String H2 = null;
                        if (line.contains("<TI>")) {
                            H2 = StringUtils.substringBetween(line, "<TI>", "</TI>");
                        } else {
                            H2 = StringUtils.substringBetween(line, "<H2>", "</H2>");
                        }

                        System.out.println("H2 is " + H2);
                        doc.get(count - 1).add(new TextField("H2", H2, Field.Store.YES));
                    } else if (line.startsWith("<H3>")) {
                        String H3 = null;
                        if (line.contains("<TI>")) {
                            if (line.contains("</TI>")) {
                                H3 = StringUtils.substringBetween(line, "<TI>", "</TI>");
                            } else {
                                H3 = line.substring(10);
                            }
                        } else if (line.contains("</H3>")) {
                            H3 = StringUtils.substringBetween(line, "<H3>", "</H3>");
                        } else {
                            H3 = line.substring(4);
                        }
                        System.out.println("H3 is " + H3);
                        doc.get(count - 1).add(new TextField("H3", H3, Field.Store.YES));
                    } else if (line.startsWith("<H4>")) {
                        String H4 = null;
                        if (line.contains("<TI>")) {
                            H4 = StringUtils.substringBetween(line, "<TI>", "</TI>");
                        } else if (line.contains("</H4>")) {
                            H4 = StringUtils.substringBetween(line, "<H4>", "</H4>");
                        } else {
                            H4 = line.substring(4);
                        }
                        System.out.println("H4 is " + H4);
                        doc.get(count - 1).add(new TextField("H4", H4, Field.Store.YES));
                    } else if (line.startsWith("<H5>")) {
                        String H5 = null;
                        if (line.contains("<TI>")) {
                            H5 = StringUtils.substringBetween(line, "<TI>", "</TI>");
                        } else if (line.contains("</H5>")) {
                            H5 = StringUtils.substringBetween(line, "<H5>", "</H5>");
                        } else {
                            H5 = line.substring(4);
                        }
                        doc.get(count - 1).add(new TextField("H5", H5, Field.Store.YES));
                    } else if (line.startsWith("<H6>")) {
                        String H6 = null;
                        if (line.contains("<TI>")) {
                            H6 = StringUtils.substringBetween(line, "<TI>", "</TI>");
                        } else if (line.contains("</H6>")) {
                            H6 = StringUtils.substringBetween(line, "<H6>", "</H6>");
                        } else {
                            H6 = line.substring(4);
                        }
                        doc.get(count - 1).add(new TextField("H6", H6, Field.Store.YES));
                    } else if (line.startsWith("<H7>")) {
                        String H7 = null;
                        if (line.contains("<TI>")) {
                            H7 = StringUtils.substringBetween(line, "<TI>", "</TI>");
                        } else if (line.contains("</H7>")) {
                            H7 = StringUtils.substringBetween(line, "<H7>", "</H7>");
                        } else {
                            H7 = line.substring(4);
                        }
                        doc.get(count - 1).add(new TextField("H7", H7, Field.Store.YES));
                    } else if (line.startsWith("<H8>")) {
                        String H8 = null;
                        if (line.contains("<TI>")) {
                            H8 = StringUtils.substringBetween(line, "<TI>", "</TI>");
                        } else if (line.contains("</H8>")) {
                            H8 = StringUtils.substringBetween(line, "<H8>", "</H8>");
                        } else {
                            H8 = line.substring(4);
                        }
                        doc.get(count - 1).add(new TextField("H8", H8, Field.Store.YES));
                    } else if (line.startsWith("<F ")) {
                        String f = null;
                        if (line.contains("</F>")) {
                            f = StringUtils.substringBetween(line, ">", "</F>");
                        } else {
                            f = line.substring(10);
                        }
                        doc.get(count - 1).add(new TextField("F", f, Field.Store.YES));
                    } else if (line.startsWith("<ABS>")) {
                        String abs = StringUtils.substringBetween(line, "<ABS>", "</ABS>");
                        doc.get(count - 1).add(new TextField("ABS", abs, Field.Store.YES));
                    } else if (line.startsWith("<FIG")) {
                        String abs = StringUtils.substringBetween(line, ">", "</FIG>");
                        doc.get(count - 1).add(new TextField("ABS", abs, Field.Store.YES));
                    } else if (line.startsWith("<HEADER>")) {
                        curr = 3;
                        stack.push(curr);
                    } else if (line.startsWith("</HEADER>")) {
                        stack.pop();
                        curr = stack.peek();
                    } else if (line.startsWith("<TR>")) {
                        String tr = line.substring(4);
                        doc.get(count - 1).add(new TextField("TR", tr, Field.Store.YES));
                        curr = 4;
                        stack.push(curr);
                    } else if (line.startsWith("</TR>")) {
                        stack.pop();
                        curr = stack.peek();
                    } else if (line.startsWith("<TXT5>")) {
                        String txt5 = line.substring(6);
                        doc.get(count - 1).add(new TextField("TXT5", txt5, Field.Store.YES));
                        curr = 5;
                        stack.push(curr);
                    } else if (line.startsWith("</TXT5>")) {
                        stack.pop();
                        curr = stack.peek();
                    } else if (curr == 2) {
                        doc.get(count - 1).add(new TextField("TEXT", line, Field.Store.YES));
                    } else if (curr == 3) {
                        doc.get(count - 1).add(new TextField("HEADER", line, Field.Store.YES));
                    } else if (curr == 4) {
                        doc.get(count - 1).add(new TextField("TR", line, Field.Store.YES));
                    } else if (curr == 5) {
                        doc.get(count - 1).add(new TextField("TXT5", line, Field.Store.YES));
                    }
                }

                //writer.addDocument(doc.get(count - 1));
                System.out.println(doc.size());
            }

        }
    }
     */

    /** Indexes a single document
    // latimes
    // /Users/siqiwei/Desktop/Assignment_Two/latimes/

    static void indexDoc(IndexWriter writer, Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            System.out.println("file = " + file);

            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            int curr = -100;
            int count = 0;
            ArrayList<Document> doc = new ArrayList<Document>();
            Document doc_0 = new Document();
            doc.add(doc_0);
            Stack<Integer> stack = new Stack<Integer>();

            while ((line = br.readLine()) != null) {
                System.out.println("index go here now, line is "+line);
                if (line.startsWith("<DOC>")){
                    //需要push document
                    System.out.println("line is "+line);
                    Document newdoc = new Document();
                    doc.add(newdoc);
                    if (count != 0){
                        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                            // New index, so we just add the document (no old document can be there):
                            System.out.println("adding " + file);
                            writer.addDocument(doc.get(count-1));
                        } else {
                            System.out.println("updating " + file);
                            writer.updateDocument(new Term("path", file.toString()), doc.get(count-1));
                        }
                        //writer.addDocument(doc.get(count-1));
                    }
                    count = count + 1;
                    curr = 1;
                    stack.push(curr);
                }
                else if (line.startsWith("</DOC>")){
                    stack.pop();
                    curr = -100;
                }

                else if (line.startsWith("<TEXT>")){
                    curr = 2;
                    stack.push(curr);
                }
                else if (line.startsWith("</TEXT>")){
                    stack.pop();
                    curr = stack.peek();
                }

                else if (line.startsWith("<SECTION>")){
                    curr = 3;
                    stack.push(curr);
                }
                else if (line.startsWith("</SECTION>")){
                    stack.pop();
                    curr = stack.peek();
                }

                else if (line.startsWith("<LENGTH>")){
                    curr = 4;
                    stack.push(curr);
                }
                else if (line.startsWith("</LENGTH>")){
                    stack.pop();
                    curr = stack.peek();
                }

                else if (line.startsWith("<BYLINE>")){
                    curr = 5;
                    stack.push(curr);
                }
                else if (line.startsWith("</BYLINE>")){
                    stack.pop();
                    curr = stack.peek();
                }

                else if (line.startsWith("<DATELINE>")){
                    curr = 6;
                    stack.push(curr);
                }
                else if (line.startsWith("</DATELINE>")){
                    stack.pop();
                    curr = stack.peek();
                }
                else if (line.startsWith("<SUBJECT>")){
                    curr = 7;
                    stack.push(curr);
                }
                else if (line.startsWith("</SUBJECT>")){
                    stack.pop();
                    curr = stack.peek();
                }
                else if (line.startsWith("<CORRECTION-DATE>")){
                    curr = 8;
                    stack.push(curr);
                }
                else if (line.startsWith("</CORRECTION-DATE>")){
                    stack.pop();
                    curr = stack.peek();
                }
                else if (line.startsWith("<CORRECTION>")){  //有问题
                    curr = 9;
                    stack.push(curr);
                }
                else if (line.startsWith("</CORRECTION>")){
                    stack.pop();
                    curr = stack.peek();
                }

                else if (line.startsWith("<GRAPHIC>")){  //有问题
                    curr = 10;
                    stack.push(curr);
                }
                else if (line.startsWith("</GRAPHIC>")){
                    stack.pop();
                    curr = stack.peek();
                }

                else if (line.startsWith("<HEADLINE")){
                    curr = 12;
                    stack.push(curr);
                }

                else if (line.startsWith("</HEADLINE>")){
                    stack.pop();
                    curr = stack.peek();
                }
                else if (line.startsWith("<TEXT")){
                    curr = 13;
                    stack.push(curr);
                }

                else if (line.startsWith("</TEXT>")){
                    stack.pop();
                    curr = stack.peek();
                }
                else if (line.startsWith("<DATE>")){
                    curr = 14;
                    stack.push(curr);
                }

                else if (line.startsWith("</DATE>")){
                    stack.pop();
                    curr = stack.peek();
                }
                else if (line.startsWith("<HT>")){
                    String ht = StringUtils.substringBetween(line, "<HT>", "</HT>");
                    doc.get(count-1).add(new TextField("HT", ht, Field.Store.YES));
                    System.out.println("ht is "+ht);
                }
                else if (line.startsWith("<AU>")){
                    String au = StringUtils.substringBetween(line, "<AU>", "</AU>");
                    doc.get(count-1).add(new TextField("AU", au, Field.Store.YES));
                    System.out.println("au is "+au);
                }
                else if (line.startsWith("<DOCNO>")){
                    String docno = StringUtils.substringBetween(line, "<DOCNO>", "</DOCNO>");
                    System.out.println("docno is "+docno);
                    doc.get(count-1).add(new TextField("DOCNO", docno, Field.Store.YES));
                }
                else if (line.startsWith("<DOCID>")){
                    String docid = StringUtils.substringBetween(line, "<DOCID>", "</DOCID>");
                    System.out.println("docid is "+docid);
                    doc.get(count-1).add(new TextField("DOCID", docid, Field.Store.YES));
                }

                else if (line.startsWith("<DATE>")){
                    String date = StringUtils.substringBetween(line, "<DATE>", "</DATE>");
                    System.out.println("date is "+date);
                    doc.get(count-1).add(new TextField("DATE", date, Field.Store.YES));
                }

                else if (curr == 2){ doc.get(count-1).add(new TextField("TEXT", line, Field.Store.YES)); }
                else if (curr == 3){
                    if (!line.startsWith("<p>") && !line.startsWith("</p>") ){
                        doc.get(count-1).add(new TextField("SECTION", line, Field.Store.YES));
                    }
                }
                else if (curr == 4) {
                    if (!line.startsWith("<p>") && !line.startsWith("</p>")) {
                        doc.get(count - 1).add(new TextField("LENGTH", line, Field.Store.YES));
                    }
                }
                else if (curr == 5) {
                    if (!line.startsWith("<p>") && !line.startsWith("</p>")) {
                        doc.get(count - 1).add(new TextField("BYLINE", line, Field.Store.YES));
                    }
                }
                else if (curr == 6) {
                    if (!line.startsWith("<p>") && !line.startsWith("</p>")) {
                        doc.get(count - 1).add(new TextField("DATELINE", line, Field.Store.YES));
                    }
                }
                else if (curr == 7) {
                    if (!line.startsWith("<p>") && !line.startsWith("</p>")) {
                        doc.get(count - 1).add(new TextField("SUBJECT", line, Field.Store.YES));
                    }
                }
                else if (curr == 8) {
                    if (!line.startsWith("<p>") && !line.startsWith("</p>")) {
                        doc.get(count - 1).add(new TextField("CORRECTION-DATE", line, Field.Store.YES));
                    }
                }
                else if (curr == 9) {
                    if (!line.startsWith("<p>") && !line.startsWith("</p>")) {
                        doc.get(count - 1).add(new TextField("CORRECTION", line, Field.Store.YES));
                    }
                }
                else if (curr == 10) {
                    if (!line.startsWith("<p>") && !line.startsWith("</p>")) {
                        doc.get(count - 1).add(new TextField("GRAPHIC", line, Field.Store.YES));
                    }
                }
                else if (curr == 11) {
                    if (!line.startsWith("<") && !line.startsWith("</") ) {
                        doc.get(count - 1).add(new TextField("TABLE", line, Field.Store.YES));
                    }
                }
                else if (curr == 12) {
                    if (!line.startsWith("<") && !line.startsWith("</") ) {
                        doc.get(count - 1).add(new TextField("HEADLINE", line, Field.Store.YES));
                    }
                }
                else if (curr == 13) {
                    if (!line.startsWith("<") && !line.startsWith("</") ) {
                        doc.get(count - 1).add(new TextField("TEXT", line, Field.Store.YES));
                    }
                }
                else if (curr == 14) {
                    if (!line.startsWith("<") && !line.startsWith("</") ) {
                        doc.get(count - 1).add(new TextField("DATE", line, Field.Store.YES));
                    }
                }

            }

            writer.addDocument(doc.get(count-1));
            System.out.println(doc.size());
        }
    */

}

