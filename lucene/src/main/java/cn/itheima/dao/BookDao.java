package cn.itheima.dao;

import java.util.List;

import cn.itheima.po.Book;

public interface BookDao {
	List<Book> queryBookList();
}
