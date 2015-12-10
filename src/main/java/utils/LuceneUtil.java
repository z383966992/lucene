package utils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneUtil {

	private static final Logger log = LoggerFactory.getLogger(LuceneUtil.class);

	private String indexPath;

//	public synchronized IndexWriter getIndexWriter() throws CorruptIndexException, LockObtainFailedException,
//			IOException {
//		IndexWriter indexWriter = null;
//
//		File indexFile = new File(indexPath);
//		if (!indexFile.exists()) {
//			indexFile.mkdirs();
//		}
//		Analyzer analyzer = getAnalyzer();
//		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_34, analyzer);
//		iwc.setOpenMode(OpenMode.CREATE);
//		indexWriter = new IndexWriter(FSDirectory.open(indexFile), iwc);
//
//		return indexWriter;
//	}

	public synchronized IndexWriter getIndexWriter(boolean isAppend) throws CorruptIndexException, LockObtainFailedException,
			IOException {
		IndexWriter indexWriter = null;

		File indexFile = new File(indexPath);
		if (!indexFile.exists()) {
			indexFile.mkdirs();
		}
		Analyzer analyzer = getAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_34, analyzer);
		if(isAppend) {
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		} else {
			iwc.setOpenMode(OpenMode.CREATE);
		}
		indexWriter = new IndexWriter(FSDirectory.open(indexFile), iwc);

		return indexWriter;
	}



	public synchronized IndexSearcher getIndexSearcher() throws CorruptIndexException, IOException {
		IndexSearcher indexSearcher = null;

		indexSearcher = new IndexSearcher(FSDirectory.open(new File(indexPath)));

		return indexSearcher;
	}

	/**
	 * 鍙栧緱瑙ｆ瀽鍣�
	 * 
	 * @return
	 */
	public Analyzer getAnalyzer() {
		return new SampleUrlAnalyzer();
	}

	public Highlighter getHighlighter(Analyzer analyzer, String keyword, String field) throws ParseException {
		Query query = null;

		query = new QueryParser(Version.LUCENE_34, field, analyzer).parse(keyword);

		Highlighter highlighter = null;
		if (query != null) {
			highlighter = new Highlighter(new SimpleHTMLFormatter("<span class='highlight'>", "</span>"),
					new QueryScorer(query));
			highlighter.setTextFragmenter(new SimpleFragmenter(100));
		}
		return highlighter;
	}

	public Highlighter getHighlighter(Query query) {
		// 楂樹寒htmlFormatter瀵硅薄
		SimpleHTMLFormatter sHtmlF = new SimpleHTMLFormatter("<b><font color='red'>", "</font></b>");
		// 楂樹寒瀵硅薄
		Highlighter highlighter = new Highlighter(sHtmlF, new QueryScorer(query));
		// 璁剧疆楂樹寒闄勮繎鐨勫瓧鏁�
		highlighter.setTextFragmenter(new SimpleFragmenter(100));
		return highlighter;
	}

	public String getHighlighterStr(Analyzer analyzer, Query query, String field, String srcStr) {
		if (StringUtils.isBlank(srcStr)) {
			return "";
		}
		Formatter formatter = new SimpleHTMLFormatter("<font color=\"red\">", "</font>");
		Highlighter highlight = new Highlighter(formatter, new QueryScorer(query));
		TokenStream tokens = analyzer.tokenStream(field, new StringReader(srcStr));

		try {
			String s = highlight.getBestFragment(tokens, srcStr);
			if (s == null) {
				return srcStr;
			} else {
				return s;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTokenOffsetsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return srcStr;
	}

	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}

}
