import java.awt.Font;

import com.google.gson.Gson;

public class GSON_Test {

	public static void main(String[] args) {
		Gson gson = new Gson();
		InterfaceSettings is = new InterfaceSettings();
		is.font = new Font("Times New Roman", 14, Font.PLAIN);
		is.fontSize = 14;
		System.out.println(gson.toJson(is));
	}

}
