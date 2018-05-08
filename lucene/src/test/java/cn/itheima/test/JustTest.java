package cn.itheima.test;


public class JustTest {
	{
		System.out.println("a");
	}
	static {
		System.out.println("b");
	}
	public JustTest(){
		System.out.println("c");
	}
	public static void main(String[] args) {
		System.out.println("d");
		new JustTest();
		System.out.println("e");
		new JustTest();
		//b d a c e a c
		
	}
}
