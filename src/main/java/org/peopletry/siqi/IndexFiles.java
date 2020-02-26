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
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import java.util.ArrayList;

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
            Analyzer analyzer = new ClassicAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            System.out.println("analyzer"+ analyzer);
            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                //iwc.setSimilarity(new BM25Similarity());

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
                        indexDoc(writer, file);
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDoc(writer, path);
        }
    }

    /** Indexes a single document */
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
            int mark = -1;
            int count = 0;
            ArrayList<Document> doc = new ArrayList<Document>();
            Document doc_0 = new Document();
            doc.add(doc_0);
            while ((line = br.readLine()) != null) {
                if (line.substring(0, 2).equals(".I")){
                    //需要push document

                    Document newdoc = new Document();
                    doc.add(newdoc);

                    if (count != 0){
                        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                            // New index, so we just add the document (no old document can be there):
                            System.out.println("adding " + file);
                            writer.addDocument(doc.get(count-1));
                        } else {
                            // Existing index (an old copy of this document may have been indexed) so
                            // we use updateDocument instead to replace the old one matching the exact
                            // path, if present:
                            System.out.println("updating " + file);
                            writer.updateDocument(new Term("path", file.toString()), doc.get(count-1));
                        }
                        //writer.addDocument(doc.get(count-1));
                    }
                    count = count + 1;
                    mark = 0;
                    //System.out.println(line);
                    String id = line.substring(3);
                    System.out.println(id);
                    doc.get(count-1).add(new TextField("ID", id, Field.Store.YES));


                }
                else if (line.substring(0,2).equals(".T")){ mark = 1;} //last line is ".T"
                else if (line.substring(0,2).equals(".A")){ mark = 2;} // last line is ".A"
                else if (line.substring(0,2).equals(".B")){ mark = 3;} //last line is ".B"

                else if (line.substring(0,2).equals(".W")){ mark = 4;} // last line is ".W"

                else if (mark == 1) {
                    doc.get(count-1).add(new TextField("title", line, Field.Store.YES));
                }
                else if (mark == 2){
                    doc.get(count-1).add(new TextField("author", line, Field.Store.YES));
                }
                else if (mark == 3){
                    doc.get(count-1).add(new TextField("bibliographic", line, Field.Store.YES));
                }
                else if (mark == 4){
                    doc.get(count-1).add(new TextField("contents", line, Field.Store.YES));
                }

            }
            writer.addDocument(doc.get(count-1));
            System.out.println(doc.size());

        }
    }
}

