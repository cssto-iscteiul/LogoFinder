
public class Tester {

	private static String[] searchTypes = {"Simple Search", "90º Search", "180º Search"};
	
	public static void main(String[] args) {
		
		System.out.println(searchTypes());

	}
	
	public static String searchTypes() {
		String str="";
		
		for(int i=0; i!=searchTypes.length; i++) {
			str= str + searchTypes[i];
			if(i!=searchTypes.length-1) {
				str = str + ",";
			} 			
		}
		return str;
	}

}
