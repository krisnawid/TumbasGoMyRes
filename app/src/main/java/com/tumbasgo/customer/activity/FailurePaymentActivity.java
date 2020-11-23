package com.tumbasgo.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.tumbasgo.customer.R;

import butterknife.ButterKnife;

public class FailurePaymentActivity extends AppCompatActivity {

    public static FailurePaymentActivity failurePaymentActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_failure_payment);
        ButterKnife.bind(this);
    }

    public static FailurePaymentActivity getInstance() {
        return failurePaymentActivity;
    }

    public void pembayaranGagalKembali(View view) {
        startActivity(new Intent(FailurePaymentActivity.this, HomeActivity.class));
    }

}
