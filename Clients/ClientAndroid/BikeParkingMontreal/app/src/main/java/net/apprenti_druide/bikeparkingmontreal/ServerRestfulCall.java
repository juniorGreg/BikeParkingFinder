package net.apprenti_druide.bikeparkingmontreal;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;



public class ServerRestfulCall extends AsyncTask<Void, Void, JSONArray> {

    public enum REQUEST {
        GET_BIKE_PARKING("geolocation?coord=[%s]"),
        GET_ALL_BIKE_PARKING("all_locations"),
        ADD_BIKE_PARKING("add_bike_parking?coord=[%s]&radius=%s"),
        CONFIRM_BIKE_PARKING("remove_bike_parking?id=%s"),
        REMOVE_BIKE_PARKING("confirm_bike_parking?id=%s");

        private String url = "";
        private final String domain = "http://192.168.1.114/bike_parking/";

        //Constructeur

        REQUEST(String url) {
            this.url = url;
        }

        public String toString() {

            return domain + url;

        }
    }


    private HttpClient mClient;
    private String requestUrl;
    private MainActivity map;



    public ServerRestfulCall(MainActivity map, REQUEST request, String... params) {
        this.map = map;
        mClient = new DefaultHttpClient();
        if(params != null)
            requestUrl = String.format(request.toString(),params);
        else
            requestUrl=request.toString();
    }

    @Override
    protected JSONArray doInBackground(Void... params) {

        JSONArray jsonArray = null;
        HttpUriRequest request = new HttpGet(requestUrl);
        try {
            HttpResponse serverResponse = mClient.execute(request);
            BasicResponseHandler handler = new BasicResponseHandler();
            String responseStr = handler.handleResponse(serverResponse);
            if(responseStr.startsWith("["))
                jsonArray= new JSONArray(responseStr);
            else
            {
                JSONObject jsonObject = new JSONObject(responseStr);
                jsonArray = new JSONArray();
                jsonArray.put(jsonObject);
            }
            //return null;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return jsonArray;
    }

    @Override
    protected void onPostExecute(JSONArray result) {
        if(result == null)
        {
            return;
        }


        ArrayList<BikeParkingLocation> bikeParkings = new ArrayList<BikeParkingLocation>();
        for(int i=0; i<result.length(); i++)
        {
            try {
                JSONObject bikeParkingJs = result.getJSONObject(i);
                if(bikeParkingJs.has("error"))
                {

                    showError(bikeParkingJs.getString("error"));
                    break;
                }

                JSONArray coordJs = bikeParkingJs.getJSONArray("coord");
                double latitude =coordJs.getDouble(0);
                double longitude =coordJs.getDouble(1);
                int status = bikeParkingJs.getInt("status");
                String id = bikeParkingJs.getString("id");
                GeoPoint pt =new GeoPoint(latitude, longitude);

                bikeParkings.add(new BikeParkingLocation(id, pt, status));


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        map.updateBikeParkings(bikeParkings);

    }

    public void showError(String error)
    {
        if(error.equals("accuracy_to_low")){

            Toast.makeText(map.getApplicationContext(), "Location Accuracy is too low!", Toast.LENGTH_LONG).show();
        }

    }
}
