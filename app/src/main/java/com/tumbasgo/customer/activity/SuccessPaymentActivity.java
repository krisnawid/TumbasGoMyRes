package com.tumbasgo.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.tumbasgo.customer.R;

import butterknife.ButterKnife;

public class SuccessPaymentActivity extends AppCompatActivity {

    public static SuccessPaymentActivity successPaymentActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_payment);
        ButterKnife.bind(this);
    }

    public static SuccessPaymentActivity getInstance() {
        return successPaymentActivity;
    }

    public void pembayaranSuksesKembali(View view) {
        startActivity(new Intent(SuccessPaymentActivity.this, HomeActivity.class));
    }
}
