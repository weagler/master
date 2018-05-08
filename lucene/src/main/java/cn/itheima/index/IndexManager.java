package cn.itheima.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import cn.itheima.dao.BookDao;
import cn.itheima.dao.impl.BookDaoImpl;
import cn.itheima.po.Book;

public class IndexManager {
	// 定义索引库位置常量
	public static final String INDEX_PATH_DIRECTORY="E:\\teach\\0344\\index\\";
	@Test
	public void createIndex() throws IOException {
		//采集数据
		BookDao dao = new BookDaoImpl();
		List<Book> bookList = dao.queryBookList();
		//创建文档对象
		List<Document> docList = new ArrayList<Document>();
		for (Book book : bookList) {
			//创建文档对象
			Document doc = new Document();
			/**
			 * 方法：add。它的作用使用把域添加到文档对象中
			 * 参数：field。域
			 * 
			 * TextField：文本域
			 * 参数：
			 * 		name：域的名称
			 * 		value：域值
			 * 		store：指定是否把域值存储到文档中
			 */
			doc.add(new StringField("bookId",book.getId()+"",Store.YES));
			doc.add(new TextField("bookName",book.getBookname(),Store.YES));
//			图书价格
			doc.add(new FloatField("bookPrice", book.getPrice(), Store.YES));
//			图书图片
			doc.add(new StoredField("bookPic", book.getPic()));
//			图书描述
			doc.add(new TextField("bookDesc", book.getBookdesc(), Store.NO));
			
			docList.add(doc);
		}
		//建立分析器对象,用于分词
		Analyzer analyzer = new IKAnalyzer();
		//建立索引库配置对象
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);
		//建立索引库目录对象,指定索引位置
		File path = new File("E:\\teach\\0344\\index\\");
		Directory directory = FSDirectory.open(path);
		//建立索引库的操作对象,操作索引库
		IndexWriter writer = new IndexWriter(directory,iwc);
		for (Document document : docList) {
			writer.addDocument(document);
		}
		//释放资源
		writer.close();
	}
	@Test
	public void readIndex() throws Exception {
		Analyzer analyzer = new IKAnalyzer();
		QueryParser qp = new QueryParser("bookName",analyzer);
		Query query = qp.parse("bookName:java");
		Directory directory = FSDirectory.open(new File(INDEX_PATH_DIRECTORY));
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs topDocs = searcher.search(query, 100);
		System.out.println("实际查询到的数量" + topDocs.totalHits);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc sd : scoreDocs) {
			System.out.println("----------------华丽丽的分割线--------------");
			int docId = sd.doc;
			float score = sd.score;
			System.out.println("文档的Id："+docId+",文档分值："+score);
			Document doc = searcher.doc(docId);
			System.out.println("图书Id："+doc.get("bookId"));
			System.out.println("图书名称："+doc.get("bookName"));
			System.out.println("图书价格："+doc.get("bookPrice"));
			System.out.println("图书图片："+doc.get("bookPic"));
			System.out.println("图书描述："+doc.get("bookDesc"));
		}
		reader.close();
	}
	
	/**
	 * 检索流程实现（分析搜索）
	 * @throws Exception 
	 */
	@Test
	public void readIndexPage() throws Exception{
//		1.建立分析器对象（Analyzer），用于分词
		//Analyzer analyzer = new StandardAnalyzer();
		
		// 使用ik中文分词器
		Analyzer  analyzer = new IKAnalyzer();
		
//		2.建立查询对象（Query）
		// 2.1.建立查询解析器对象【bookName:java】
		/**
		 * 参数一：默认搜索域
		 * 参数二：使用的分析器对象
		 */
		QueryParser qp = new QueryParser("bookName", analyzer);
		
		// 2.2.使用查询解析器对象，解析表达式，实例化query对象
		Query query = qp.parse("bookName:java");
		
		
//		3.建立索引库的目录对象（Directory），指定索引库的位置
		Directory directory = FSDirectory.open(new File(INDEX_PATH_DIRECTORY));
		
//		4.建立索引的读取对象（IndexReader），把索引数据读取到内存中
		IndexReader reader = DirectoryReader.open(directory);
		
//		5.建立索引搜索对象（IndexSearcher），执行搜索，返回搜索结果集（TopDocs）
		IndexSearcher searcher = new IndexSearcher(reader);
		
		/**
		 * 方法search：执行搜索
		 * 参数一：查询对象
		 * 参数二：指定搜索结果排序后的前n个
		 */
		TopDocs topDocs = searcher.search(query, 10);
		
//		6.处理结果集
		// 6.1.实际查询到的结果数量
		System.out.println("实际查询到的结果数量："+topDocs.totalHits);
		
		// 6.2.获取查询的结果数组
		/**
		 * ScoreDoc对象中只包含两个信息：
		 * 	1.文档的id
		 * 	2.文档的分值
		 */
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		
		// 增加分页搜索处理==========================start
		// 1.当前页
		//int page=1;//默认查询第一页
		int page=1;
		
		// 2.每一页显示大小
		int pageSize=2;
		
		// 3.当前页的开始索引
		int start=(page-1)*pageSize;
		
		// 4.当前页的结束索引
		// 正常情况下：start+pageSize
		// 最后一页：start+pageSize 和scoreDocs.length取最小值
		int end=Math.min(start+pageSize, scoreDocs.length);
		
		// 增加分页搜索处理==========================end
		//for(ScoreDoc sd:scoreDocs){
		for(int i=start;i<end;i++){
			
			System.out.println("----------------华丽丽的分割线--------------");
			// 获取文档id和分值
			int docId = scoreDocs[i].doc;
			float score = scoreDocs[i].score;
			System.out.println("文档的Id："+docId+",文档分值："+score);
			
			// 根据文档id获取文档的数据（相当于关系数据库表中，根据主键值查询）
			Document doc = searcher.doc(docId);
			System.out.println("图书Id："+doc.get("bookId"));
			System.out.println("图书名称："+doc.get("bookName"));
			System.out.println("图书价格："+doc.get("bookPrice"));
			System.out.println("图书图片："+doc.get("bookPic"));
			System.out.println("图书描述："+doc.get("bookDesc"));
			
		}
		
//		7.释放资源
		reader.close();
	}
	/**
	 *
	* @Title: deleteIndexByTerm 
	* @Description: 根据Term删除索引
	* @throws Exception     
	* @return void     
	* @throws
	 */
	@Test
	public void deleteIndexByTerm() throws Exception{
		Analyzer analyzer = new IKAnalyzer();
		//建立索引为配置对象
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);
		Directory directory = FSDirectory.open(new File(INDEX_PATH_DIRECTORY));
//		4.建立索引库操作对象（IndexWriter），用于操作索引库
		IndexWriter writer = new IndexWriter(directory, iwc);
		
//		5.建立条件对象（Term）
		// delete from 表 where 字段=值
		// 删除图书名称域中包含有lucene的图书
		Term term = new Term("bookName", "lucene");
		
//		6.使用IndexWriter对象，执行删除
		writer.deleteDocuments(term);
		
//		7.释放资源
		writer.close();
	}
	
	/**
	 * 使用TermQuery
	 * 		需求：查询图书名称域中有java图书。
	 * @throws Exception 
	 */
	@Test
	public void testTermQuery() throws Exception{
		// 1.建立查询对象
		// bookName:java
		TermQuery tq = new TermQuery(new Term("bookName","java"));
		
		// 2.执行搜索
		this.searcher(tq);
		
	}
	
	/**
	 * 封装搜索方法
	 * @throws Exception 
	 */
	private void searcher(Query query) throws Exception{
		
		// 打印Query对象生成的查询语法
		System.out.println("查询语法："+query);
	
//		1.建立索引库目录对象（Directory），指定索引库的位置
		Directory directory = FSDirectory.open(new File(INDEX_PATH_DIRECTORY));
		
//		2.建立索引读取对象（IndexReader），用于把索引数据读取到内存中
		IndexReader reader = DirectoryReader.open(directory);
		
//		3.建立索引搜索对象（IndexSearcher），用于执行搜索
		IndexSearcher searcher = new IndexSearcher(reader);
		
//		4.使用IndexSearcher对象，执行搜索，返回搜索结果集TopDocs
		// search方法：执行搜索
		// 参数query：查询对象
		// 参数n：指定搜索结果排序以后的前n个
		TopDocs topDoc = searcher.search(query, 10);
		
//		5.处理结果集
		// 5.1打印实际搜索到的结果数量
		System.out.println("实际搜索到的结果数量："+topDoc.totalHits);
		
		// 5.2获取搜索结果的文档分值对象
		// 这里包含两个信息：一个是文档id；另一个文档的分值
		ScoreDoc[] scoreDocs = topDoc.scoreDocs;
		for(ScoreDoc sd:scoreDocs){
			System.out.println("-------------------------------");
			// 获取文档id和文档分值
			int docId = sd.doc;
			float score = sd.score;
			System.out.println("当前文档id："+docId+",当前文档分值:"+score);
			
			// 根据文档id取数据（相当于关系数据库中根据主键查询）
			Document doc = searcher.doc(docId);
			System.out.println("图书id："+doc.get("bookId"));
			System.out.println("图书名称："+doc.get("bookName"));
			System.out.println("图书价格："+doc.get("bookPrice"));
			System.out.println("图书图片："+doc.get("bookPic"));
			System.out.println("图书描述："+doc.get("bookDesc"));
		}
		
//		6.释放资源
		reader.close();
	}
}
