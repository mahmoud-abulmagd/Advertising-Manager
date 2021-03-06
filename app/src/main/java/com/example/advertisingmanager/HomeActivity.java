package com.example.advertisingmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeActivity extends AppCompatActivity {

    String avatarURL;
    private ImageView img_avatar;
    private TextView tv_name;
    private TextView tv_email;
    private TextView tv_bio;
    private TextView et_balance_count;

    private Adapter adapter;
    private ArrayList<DataTemplate> myDataTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        RecyclerView myRecyclerView = findViewById(R.id.recycler_view);

        img_avatar = findViewById(R.id.img_avatar);
        tv_name = findViewById(R.id.tv_name);
        tv_email = findViewById(R.id.tv_email);
        tv_bio = findViewById(R.id.tv_bio);
        et_balance_count = findViewById(R.id.tv_balance_count);

        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        myDataTemplate = new ArrayList<>();
        adapter = new Adapter(HomeActivity.this, myDataTemplate);
        myRecyclerView.setAdapter(adapter);

        fetchProfileData();

    }

    public void addCampaign(View view) {
        Intent intent = new Intent(this, AddCampaignActivity.class);
        intent.putExtra("budget", et_balance_count.getText().toString());
        startActivity(intent);
    }

    public void logout(View view) {
        SessionManager manager = new SessionManager();
        manager.logout();
        finish();
        startActivity(new Intent(this, WelcomeScreenActivity.class));
    }

    public void settings(View view) {
        Intent intent = new Intent(this, ChangeAccountDataActivity.class);
        intent.putExtra("username", tv_name.getText());
        intent.putExtra("email", tv_email.getText());
        intent.putExtra("bio", tv_bio.getText());
        intent.putExtra("avatar", avatarURL);
        startActivity(intent);
    }

    private void fetchProfileData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        final SessionManager manager = SessionManager.getInstance(this);

        JsonObjectRequest campaignDataReq = new JsonObjectRequest(Request.Method.GET,
                "https://stark-ridge-68501.herokuapp.com/campaigns",
                new JSONObject(),
                response -> {
                    try {
                        Log.e("RES::", response.toString());
                        if (response.getBoolean("success")) {
                            JSONArray jsonArray = response.getJSONArray("rows");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject campaigns = jsonArray.getJSONObject(i);

                                int campaignId = Integer.parseInt(campaigns.getString("id"));
                                String campaignName = campaigns.getString("name");
                                String campaignLink = campaigns.getString("link");
                                float budget = Float.parseFloat(campaigns.getString("budget"));
                                float clickPrice = Float.parseFloat(campaigns.getString("click_price"));

                                myDataTemplate.add(new DataTemplate(campaignId, campaignName,
                                        campaignLink, budget, clickPrice));
                                adapter.notifyDataSetChanged();
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(HomeActivity.this,
                                "Loading has failed, check your internet connection!",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(HomeActivity.this,
                            "Loading has failed, check your internet connection!",
                            Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> auth = new HashMap<>();
                auth.put("Authorization", manager.getToken());
                return auth;
            }
        };
        queue.add(campaignDataReq);

        JsonObjectRequest profileDataReq
                = new JsonObjectRequest(Request.Method.GET,
                "https://stark-ridge-68501.herokuapp.com/advertisers/me", new JSONObject(), response ->
        {
            try {
                if (response.getBoolean("success")) {
                    Picasso
                            .get()
                            .load("https://o6ugproject.s3.amazonaws.com/"
                                    + response.getJSONObject("advertiser")
                                    .getString("avatar"))
                            .noFade()
                            .into(img_avatar);

                    tv_name.setText(response.getJSONObject("advertiser").getString("name"));
                    tv_email.setText(response.getJSONObject("advertiser").getString("email"));
                    tv_bio.setText(response.getJSONObject("advertiser").getString("bio"));
                    //password = response.getJSONObject("advertiser").getString("password");
                    avatarURL = response.getJSONObject("advertiser").getString("avatar");
                    et_balance_count.setText(" " + response.getJSONObject("advertiser").getString("balance") + "LE");

                }
                else {
                    Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error ->
        {
            VolleyLog.d("Error: ", error.getMessage());
            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
        }) {
            // Passing request headers

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> auth = new HashMap<>();
                auth.put("Authorization", manager.getToken());
                return auth;
            }
        };

        // Adding request to request queue
        queue.add(profileDataReq);
    }
}