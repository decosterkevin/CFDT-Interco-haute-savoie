package decoster.cfdt.helper;

import android.util.Log;

import decoster.cfdt.activity.MainActivity;

/**
 * Created by Decoster on 26/05/2016.
 */
public class AddressParser {
    public static String[] parseText(String text) {

        text = text.replaceAll(",", " ").replaceAll("\n", " ");
        text = text.trim().replaceAll(" +", " ");
        Log.d("TAG3", text);
        String[] text_list = text.split(" ");
        String postal_code = "";
        String adress = "";
        String city = "";
        for (int i = 0; i < text_list.length; i++) {
            String elem = text_list[i];
            Log.d(MainActivity.class.getSimpleName(), elem);
            if (elem.matches("\\d+(?:\\.\\d+)?") && (elem.length() == 5)) {
                Log.d(MainActivity.class.getSimpleName(), "postal" + elem);
                //find the postal <code>
                postal_code = elem;
                String tmp = "";
                int j = i - 1;
                while (!tmp.matches("\\d+(?:\\.\\d+)?") && j >= 0) {
                    tmp = text_list[j];
                    Log.d(MainActivity.class.getSimpleName(), "adress" + tmp);
                    adress = text_list[j] + " " + adress;
                    j--;
                    if (tmp.equals("=")) {
                        break;
                    }
                }

                if (i + 1 < text_list.length) {
                    city = text_list[i + 1];
                }
                break;
            }
        }
        adress = adress.replaceAll("[^a-zA-Z0-9éàèçù]", " ") ;
        postal_code = postal_code.replaceAll("[^a-zA-Z0-9 ]", " ").replaceAll("\\s+","");
        city = city.replaceAll("[^a-zA-Z ]", " ").replaceAll("\\s+","");
        return new String[]{adress, postal_code, city};
    }
}
