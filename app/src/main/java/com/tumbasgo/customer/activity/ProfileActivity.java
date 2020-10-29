package com.tumbasgo.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.tumbasgo.customer.R;
import com.tumbasgo.customer.model.CCode;
import com.tumbasgo.customer.model.Contry;
import com.tumbasgo.customer.model.LoginUser;
import com.tumbasgo.customer.model.User;
import com.tumbasgo.customer.retrofit.APIClient;
import com.tumbasgo.customer.retrofit.GetResult;
import com.tumbasgo.customer.utils.SessionManager;
import com.tumbasgo.customer.utils.Utiles;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;

public class ProfileActivity extends BaseActivity implements GetResult.MyListener {


    SessionManager sessionManager;
    User user;
    @BindView(R.id.ed_username)
    EditText edUsername;
    @BindView(R.id.ed_email)
    EditText edEmail;
    @BindView(R.id.ed_alternatmob)
    EditText edAlternatmob;
    @BindView(R.id.ed_password)
    EditText edPassword;
    @BindView(R.id.spinner)
    Spinner spinner;
    List<CCode> cCodes = new ArrayList<>();
    String codeSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        sessionManager = new SessionManager(ProfileActivity.this);
        user = sessionManager.getUserDetails("");
        setcountaint(user);
        getCode();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                codeSelect = cCodes.get(position).getCcode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setcountaint(User user) {
        edUsername.setText("" + user.getName());
        edEmail.setText("" + user.getEmail());
        edAlternatmob.setText("" + user.getMobile());
        edPassword.setText("" + user.getPassword());
    }

    @OnClick(R.id.txt_save)
    public void onViewClicked() {
        if (validation()) {

            if (edAlternatmob.getText().toString().equalsIgnoreCase(user.getMobile()) && codeSelect.equalsIgnoreCase(user.getCcode())) {
                updateUser();
            } else {
                Utiles.isvarification = 2;
                User user1 = new User();
                user1.setId(user.getId());
                user1.setEmail(edEmail.getText().toString());
                user1.setMobile(edAlternatmob.getText().toString());
                user1.setName(edUsername.getText().toString());
                user1.setPassword(edPassword.getText().toString());
                user1.setCcode(codeSelect);
                startActivity(new Intent(ProfileActivity.this, VerifyPhoneActivity.class).putExtra("code", codeSelect).putExtra("phone", edAlternatmob.getText().toString()).putExtra("user", user1));

            }
        }
    }

    private void updateUser() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            jsonObject.put("name", edUsername.getText().toString());
            jsonObject.put("email", edEmail.getText().toString());
            jsonObject.put("mobile", edAlternatmob.getText().toString());
            jsonObject.put("ccode", codeSelect);
            jsonObject.put("password", edPassword.getText().toString());
            jsonObject.put("imei", Utiles.getIMEI(ProfileActivity.this));
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().updateProfile((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCode() {
        JSONObject jsonObject = new JSONObject();
        JsonParser jsonParser = new JsonParser();
        Call<JsonObject> call = APIClient.getInterface().getCode((JsonObject) jsonParser.parse(jsonObject.toString()));
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "2");

    }

    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                LoginUser response = gson.fromJson(result.toString(), LoginUser.class);
                Toast.makeText(ProfileActivity.this, "" + response.getResponseMsg(), Toast.LENGTH_LONG).show();
                if (response.getResult().equals("true")) {
                    sessionManager.setUserDetails("", response.getUser());
                    startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                    finish();
                }
            } else if (callNo.equalsIgnoreCase("2")) {
                Gson gson = new Gson();
                Contry contry = gson.fromJson(result.toString(), Contry.class);
                cCodes = contry.getData();
                List<String> arrayList = new ArrayList<>();
                for (int i = 0; i < cCodes.size(); i++) {
                    if (cCodes.get(i).getStatus().equalsIgnoreCase("1")) {
                        arrayList.add(cCodes.get(i).getCcode());
                    }
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(dataAdapter);
                int spinnerPosition = dataAdapter.getPosition(user.getCcode());
                spinner.setSelection(spinnerPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean validation() {
        if (edUsername.getText().toString().isEmpty()) {
            edUsername.setError("Masukan Nama");
            return false;
        }

        if (edEmail.getText().toString().isEmpty()) {
            edEmail.setError("Masukan Email yang Valid");
            return false;
        }
        if (edAlternatmob.getText().toString().isEmpty()) {
            edAlternatmob.setError("Masukan Nomor Telepon dengan Benar");
            return false;
        }
        return true;
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }


}
