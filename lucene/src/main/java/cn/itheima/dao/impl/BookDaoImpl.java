package cn.itheima.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


import cn.itheima.dao.BookDao;
import cn.itheima.po.Book;

public class BookDaoImpl implements BookDao {

	public List<Book> queryBookList() {
		List<Book> bookList = new ArrayList<Book>();
		Connection conn = null;
		PreparedStatement psmt = null;
		ResultSet rs = null;
		try{
			//加载驱动
			Class.forName("com.mysql.jdbc.Driver");
			//创建连接对象
			conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/crm", "root", "root");
			//定义sql语句
			String sql = "select *from book";
			//创建语句对象
			psmt = conn.prepareStatement(sql);
			//执行查询
			rs = psmt.executeQuery();
			while(rs.next()) {
				Book book = new Book();
				book.setId(rs.getInt("id"));
				book.setBookname(rs.getString("bookName"));
				book.setPrice(rs.getFloat("price"));
//				图书图片
				book.setPic(rs.getString("pic"));
//				图书描述
				book.setBookdesc(rs.getString("bookdesc"));
				bookList.add(book);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(psmt != null) {
					psmt.close();
				}
				if(conn != null) {
					conn.close();
				}
			}catch(Exception e) {
				
			}
		}
		
		return bookList;
	}

	
	/**
	 * 测试
	 */
	public static void main(String[] args) {
		BookDao bookDao = new BookDaoImpl();
		List<Book> list = bookDao.queryBookList();
		for(Book book:list){
			System.out.println(book);
		}
	}
}
