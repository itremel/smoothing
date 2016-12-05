package SmoothingSearch;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LMSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;


public class LMMercer {

	public static void search(String indexDir,String q, int topK, float lambda) throws IOException, ParseException {
		Path path = Paths.get(indexDir);
		IndexReader rdr = DirectoryReader.open(FSDirectory.open(path));
		Similarity sim= new LMSimilarity() {
			
			@Override
			protected float score(BasicStats stats, float freq, float doclen) {
//				System.out.println(stats.getDocFreq()+" term freq "+stats.getTotalTermFreq()+ " f: "+ stats.getNumberOfDocuments());
				return lambda*freq/doclen + 
						(1-lambda)*collectionModel.computeProbability(stats);
			}
			
			@Override
			public String getName() {
				return "custom jelinekmercer";
			}
		};
//		sync3-20120203040101_756
//		sync3-20120405040101_951
//		lk-20120110040101_1415
//		lk-20111104040101_782
//		lk-20111105040101_1312
		
		IndexSearcher is = new IndexSearcher(rdr);
//		Similarity sim = new LMJelinekMercerSimilarity(lambda);
		is.setSimilarity(sim);
		QueryParser parser =new QueryParser("contents",	new StandardAnalyzer());
		
		TopDocs hits;
		if(q.matches(".+@\\d{4}-\\d{4}")){
			String[] split = q.split("@");
			String keyword = split[0];
			String[] years = split[1].split("-");
			String start = years[0];
			String end = years[1];
	
			Query query = parser.parse(keyword);
			
			QueryParser dateparser = new QueryParser("date", new StandardAnalyzer());
			Query datequery = dateparser.parse("["+start+" TO "+end+"]");
			
			Builder bq= new BooleanQuery.Builder();
			bq.add(query, BooleanClause.Occur.MUST);
			bq.add(datequery, BooleanClause.Occur.MUST);
			
			hits = is.search(bq.build(), topK);
//			System.out.println(is.count(bq.build()));
//			System.out.println(is.explain(bq.build(),37440));
		}else{
			Query query = parser.parse(q);
			hits = is.search(query,10);
		}
		for(ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = is.doc(scoreDoc.doc);
			System.out.println(doc.get("id"));
			
		}
	}
   
	public static void main(String[] args) {
		int topK = 0;
		String query = "";
		float parameter = 0.0f;

		for(int i = 0; i< args.length-2; i++){
			query = query.concat(args[i]);
			if(i<args.length-3)
				query = query.concat(" ");
		}
//		System.out.println(query);
		topK = Integer.parseInt(args[args.length-2]);
		parameter = Float.parseFloat(args[args.length-1]);
		
		String indexdir = "C:\\workspace_java1_8\\TIR_Index_JelinekMercer";
		
		try{
			LMMercer.search(indexdir, "donald trump@2011-2013", 5, 0.3f);
//			LMMercer.search(indexdir, query, topK, parameter);
		} catch (Exception e){
			e.printStackTrace();
			
		}
	}

}