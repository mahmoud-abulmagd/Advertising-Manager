package com.example.advertisingmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class ChangeAccountDataActivity extends AppCompatActivity {

    Bundle extras;
    private ImageView img_avatar;
    private EditText et_name;
    private EditText et_email;
    private EditText et_bio;
    public final int IMG_REQUEST = 1;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_account_data);
        img_avatar = findViewById(R.id.img_avatar);
        et_name = findViewById(R.id.et_name);
        et_email = findViewById(R.id.et_email);
        et_bio = findViewById(R.id.et_bio);

        // Adding underline to the text buttons
        TextView tv_upload_image = findViewById(R.id.tv_upload_image);
        tv_upload_image.setPaintFlags(tv_upload_image.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        extras = getIntent().getExtras();
        if (extras != null) {
            et_name.setText(extras.getString("username"));
            et_email.setText(extras.getString("email"));
            et_bio.setText(extras.getString("bio"));
            Picasso.get().load("https://o6ugproject.s3.amazonaws.com/"
                    + extras.getString("avatar")).into(img_avatar);
        }

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Updating profile...");

        // Validate the input fields
        dataValidation();
    }

    public void confirm(View view) {
        String nameRegex = "^[a-zA-Z0-9_-]{3,16}$";
        if (et_name.getText().toString().matches(nameRegex)
                && Patterns.EMAIL_ADDRESS.matcher(et_email.getText().toString()).matches())
           makeChangeDataRequest();
        else
            Toast.makeText(this, "Invalid Data", Toast.LENGTH_SHORT).show();
    }

    public void back(View view) {
        finish();
    }

    public void changePassword(View view) {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        finish();
        startActivity(intent);
    }

    private void dataValidation() {

        // Alphanumeric string that may include _ and – having a length of 3 to 16 characters.
        String nameRegex = "^[a-zA-Z0-9_-]{3,16}$";

        et_name.addTextChangedListener(new TextChangedListener<EditText>(et_name) {
            @Override
            public void onTextChanged(EditText target, Editable s) {
                if (et_name.getText().toString().matches(nameRegex)) {

                    et_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person, 0,
                            R.drawable.ic_check_green, 0);
                }
                else {
                    et_name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person, 0,
                            R.drawable.ic_false, 0);
                }
            }
        });

        et_email.addTextChangedListener(new TextChangedListener<EditText>(et_email) {
            @Override
            public void onTextChanged(EditText target, Editable s) {
                if (Patterns.EMAIL_ADDRESS.matcher(et_email.getText().toString()).matches()) {

                    et_email.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0,
                            R.drawable.ic_check_green, 0);
                }
                else {
                    et_email.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email, 0,
                            R.drawable.ic_false, 0);
                }
            }
        });
    }

    private void makeChangeDataRequest() {
        dialog.show();
        final SessionManager manager = SessionManager.getInstance(this);
        String url = "https://stark-ridge-68501.herokuapp.com/advertisers/me";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.PATCH,
                url,
                response -> {
                    dialog.dismiss();
                    String resultResponse = new String(response.data);
                    try {
                        JSONObject result = new JSONObject(resultResponse);
                        boolean status = result.getBoolean("success");
                        String message = result.getString("message");

                        if (status) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            Log.i("Messsage", message);
                        } else {
                            Log.i("Unexpected", message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
        }, error -> {
            dialog.dismiss();
            NetworkResponse networkResponse = error.networkResponse;
            String errorMessage = "Unknown error";
            if (networkResponse == null) {
                if (error.getClass().equals(TimeoutError.class)) {
                    errorMessage = "Request timeout";
                } else if (error.getClass().equals(NoConnectionError.class)) {
                    errorMessage = "Failed to connect server";
                }
            } else {
                String result = new String(networkResponse.data);
                try {
                    JSONObject response = new JSONObject(result);
                    String message = response.getString("message");

                    Log.e("Error Message", message);

                    if (networkResponse.statusCode == 404) {
                        errorMessage = "Resource not found";
                    } else if (networkResponse.statusCode == 401) {
                        errorMessage = message+" Please login again";
                    } else if (networkResponse.statusCode == 400) {
                        errorMessage = message+ " Check your inputs";
                    } else if (networkResponse.statusCode == 500) {
                        errorMessage = message+" Something is getting wrong";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.i("Error", errorMessage);
            error.printStackTrace();
        })
        {
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("avatar", new DataPart("file_avatar.jpg", AppHelper.getFileDataFromDrawable(getBaseContext(), img_avatar.getDrawable()), "image/jpeg"));

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> auth = new HashMap<>();
                auth.put("Authorization", manager.getToken());
                return auth;
            }
        };

        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);


        RequestQueue queue = Volley.newRequestQueue(this);
        Map<String, String> postParam = new HashMap<>();
        postParam.put("name", et_name.getText().toString());
        postParam.put("email", et_email.getText().toString());
        postParam.put("bio", et_bio.getText().toString());

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.PATCH,
                "https://stark-ridge-68501.herokuapp.com/advertisers/me",
                new JSONObject(postParam), response -> {
            try {
                if (response.getBoolean("success")) {
                    Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
                    finish();
                    startActivity(new Intent(this, HomeActivity.class));
                } else {
                    Log.e("error", response.getString("error"));
                    Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {
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
        queue.add(jsonObjReq);
    }

    public void selectImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if (requestCode == IMG_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                img_avatar.setImageBitmap(bitmap);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}