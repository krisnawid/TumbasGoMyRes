package com.tumbasgo.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.tumbasgo.customer.R;

import butterknife.ButterKnife;

public class PendingPaymentActivity extends AppCompatActivity {

    public static PendingPaymentActivity pendingPaymentActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_payment);
        ButterKnife.bind(this);
    }

    public static PendingPaymentActivity getInstance() {
        return pendingPaymentActivity;
    }

    public void pembayaranPendingKembali(View view) {
        startActivity(new Intent(PendingPaymentActivity.this, HomeActivity.class));
    }

}
