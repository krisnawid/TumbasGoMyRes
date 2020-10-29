package com.tumbasgo.customer.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.tumbasgo.customer.R;
import com.tumbasgo.customer.utils.SessionManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PrivecyPolicyActivity extends BaseActivity {

    @BindView(R.id.txt_privacy)
    TextView txtPrivacy;
    SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privecypolicy);
        ButterKnife.bind(this);
        sessionManager = new SessionManager(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtPrivacy.setText(Html.fromHtml(sessionManager.getStringData(SessionManager.privacy), Html.FROM_HTML_MODE_COMPACT));
        } else {
            txtPrivacy.setText(Html.fromHtml(sessionManager.getStringData(SessionManager.privacy)));
        }
    }
}
