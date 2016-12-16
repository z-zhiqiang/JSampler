package test;
public class HelloWorld {

	private int field;
	private int field2;

	public HelloWorld(){
		
	}
	public HelloWorld(int i) {
		field = i;
		
	}

	public void invoke(int i) {
		if (i == 1)
			hello();
		else
			nohello();
		
		this.field2 = this.getField();

	}

	public void invoke3() {
		System.out.println(this.field);
	}

	public void hello() {
		System.out.println("Hello world!");
	}

	public void nohello() {
		System.out.println();
	}
	
	public int getField() {
		return field;
	}
	public void setField(int field) {
		this.field = field;
	}
	
	
}
