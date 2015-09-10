
public class test {
	private int field;

	public static void main(String[] args) {
		int i = Integer.parseInt(args[0]);
		int j = 10;
		int two = 2;
		
		while (j > 0 ){
			j = i - 1;
			if(i % 2 == 0){
				int z1 =0;
				if(z1 > 0){
					two++;
				}
				else{
					two--;
				}
				i=1;
				j = i + two;
			}
		}

		test t = new test();
		t.field = i + 4;
		j = t.field - two;
	
		int[] array = new int[2];
		array[0] = t.field;
		array[1] = j;
		System.out.println(array);
		
		switch (i){
		case 0: break;
		case 1: i = i+1; break;
		case 3: j = i+1; break;
		case 2: two = i+1; break;
		}
	}

}
