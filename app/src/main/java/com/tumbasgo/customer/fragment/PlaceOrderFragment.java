package com.tumbasgo.customer.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.TransactionRequest;
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme;
import com.midtrans.sdk.corekit.models.BankType;
import com.midtrans.sdk.corekit.models.CustomerDetails;
import com.midtrans.sdk.corekit.models.ItemDetails;
import com.midtrans.sdk.corekit.models.snap.Authentication;
import com.midtrans.sdk.corekit.models.snap.CreditCard;
import com.midtrans.sdk.corekit.models.snap.TransactionResult;
import com.midtrans.sdk.uikit.SdkUIFlowBuilder;
import com.tumbasgo.customer.BuildConfig;
import com.tumbasgo.customer.R;
import com.tumbasgo.customer.activity.FailurePaymentActivity;
import com.tumbasgo.customer.activity.HomeActivity;
import com.tumbasgo.customer.activity.PendingPaymentActivity;
import com.tumbasgo.customer.activity.SuccessPaymentActivity;
import com.tumbasgo.customer.database.DatabaseHelper;
import com.tumbasgo.customer.database.MyCart;
import com.tumbasgo.customer.model.Payment;
import com.tumbasgo.customer.model.PaymentItem;
import com.tumbasgo.customer.model.Times;
import com.tumbasgo.customer.model.User;
import com.tumbasgo.customer.retrofit.APIClient;
import com.tumbasgo.customer.retrofit.GetResult;
import com.tumbasgo.customer.utils.CustPrograssbar;
import com.tumbasgo.customer.utils.SessionManager;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;

public class PlaceOrderFragment extends Fragment implements View.OnClickListener, GetResult.MyListener, TransactionFinishedCallback {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    DatabaseHelper databaseHelper;
    List<MyCart> myCarts;

    User user;

    @BindView(R.id.radiogroup)
    RadioGroup rdgTime;
    Unbinder unbinder;
    @BindView(R.id.txt_selectdate)
    TextView txtSelectdate;
    @BindView(R.id.lvl_paymnet)
    LinearLayout lvlPaymnet;
    int day = 1;
    CustPrograssbar custPrograssbar;
    SessionManager sessionManager;

    public PlaceOrderFragment() {
        // Required empty public constructor
    }

    public static PlaceOrderFragment newInstance(String param1, String param2) {
        PlaceOrderFragment fragment = new PlaceOrderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initMidtransSDK();
    }

    private void initMidtransSDK() {
        SdkUIFlowBuilder.init()
                .setContext(getActivity())
                .setMerchantBaseUrl(BuildConfig.BASE_URL)
                .setClientKey(BuildConfig.CLIENT_KEY)
                .setTransactionFinishedCallback(this)
                .enableLog(true)
                .setColorTheme(new CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
                .buildSDK();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plase_order, container, false);
        unbinder = ButterKnife.bind(this, view);
        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(getActivity());

//        databaseHelper = new DatabaseHelper(getActivity());
//        myCarts = new ArrayList<>();
//        Cursor res = databaseHelper.getAllData();
//
//        while (res.moveToNext()) {
//            MyCart rModel = new MyCart();
//            rModel.setId(res.getString(0));
//            rModel.setPid(res.getString(1));
//            rModel.setImage(res.getString(2));
//            rModel.setTitle(res.getString(3));
//            rModel.setWeight(res.getString(4));
//            rModel.setCost(res.getString(5));
//            rModel.setQty(res.getString(6));
//            rModel.setDiscount(res.getInt(7));
//            myCarts.add(rModel);
//        }
//
//        System.out.println(myCarts.size());
//
//        for (int counter = 0; counter < myCarts.size(); counter++) {
//            System.out.println("CARTTTTT" + myCarts.get(counter).getPid());
//            System.out.println("CARTTTTT" + myCarts.get(counter).getTitle());
//            System.out.println("CARTTTTT" + myCarts.get(counter).getCost());
//            System.out.println("CARTTTTT" + myCarts.get(counter).getQty());
//            System.out.println("--------");
//
//
//        }
//        user = sessionManager.getUserDetails("");
//        System.out.println("Email : " + user.getEmail());
//        System.out.println("First name : " + user.getName());
//        System.out.println("Phone : " +user.getCcode() + user.getMobile());

        getTimeSlot();
        txtSelectdate.setText("" + getCurrentDate());
        HomeActivity.getInstance().setFrameMargin(0);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        HomeActivity.getInstance().serchviewHide();
        HomeActivity.getInstance().setFrameMargin(0);
    }

    private void getTimeSlot() {
        custPrograssbar.prograssCreate(getActivity());
        JSONObject jsonObject = new JSONObject();
        JsonParser jsonParser = new JsonParser();
        Call<JsonObject> call = APIClient.getInterface().getTimeslot((JsonObject) jsonParser.parse(jsonObject.toString()));
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "1");
    }

    private void getPayment() {

        JSONObject jsonObject = new JSONObject();
        JsonParser jsonParser = new JsonParser();
        Call<JsonObject> call = APIClient.getInterface().getpaymentgateway((JsonObject) jsonParser.parse(jsonObject.toString()));
        GetResult getResult = new GetResult();
        getResult.setMyListener(this);
        getResult.callForLogin(call, "2");
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            if (callNo.equalsIgnoreCase("1")) {
                RadioButton rdbtn = null;
                Log.e("Response", "->" + result);
                Gson gson = new Gson();
                Times times = gson.fromJson(result.toString(), Times.class);
                for (int i = 0; i < times.getData().size(); i++) {
                    rdbtn = new RadioButton(getActivity());
                    rdbtn.setId(View.generateViewId());
                    rdbtn.setText(times.getData().get(i).getMintime() + " - " + times.getData().get(i).getMaxtime());
                    rdbtn.setOnClickListener(this);
                    rdgTime.addView(rdbtn);
                }
                rdgTime.check(rdbtn.getId());
                getPayment();
            } else if (callNo.equalsIgnoreCase("2")) {
                custPrograssbar.closePrograssBar();
                Gson gson = new Gson();
                Payment payment = gson.fromJson(result.toString(), Payment.class);
                setJoinPlayrList(lvlPaymnet, payment.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setJoinPlayrList(LinearLayout lnrView, List<PaymentItem> paymentList) {
        lnrView.removeAllViews();
        for (int i = 0; i < paymentList.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            PaymentItem paymentItem = paymentList.get(i);
            View view = inflater.inflate(R.layout.custome_paymen, null);
            ImageView imageView = view.findViewById(R.id.img_icon);
            TextView txt_title = view.findViewById(R.id.txt_title);
            txt_title.setText("" + paymentList.get(i).getTitle());
            Glide.with(getActivity()).load(APIClient.baseUrl + "/" + paymentList.get(i).getImg()).thumbnail(Glide.with(getActivity()).load(R.drawable.ezgifresize)).into(imageView);
            String getId = APIClient.baseUrl + paymentList.get(i).getId();
            String[] id = getId.split("/");
            Integer validId = Integer.parseInt(id[4]);


            if (validId == 3) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {

                            int selectedId = rdgTime.getCheckedRadioButtonId();
                            RadioButton selectTime = rdgTime.findViewById(selectedId);
                            sessionManager.setIntData(SessionManager.COUPON, 0);
                            OrderSumrryFragment fragment = new OrderSumrryFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("DATE", txtSelectdate.getText().toString());
                            bundle.putString("TIME", selectTime.getText().toString());
                            bundle.putString("PAYMENT", paymentItem.getTitle());
                            bundle.putSerializable("PAYMENTDETAILS", paymentItem);
                            fragment.setArguments(bundle);
                            HomeActivity.getInstance().callFragment(fragment);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            else{
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MidtransSDK.getInstance().setTransactionRequest(transactionRequest());
                        MidtransSDK.getInstance().startPaymentUiFlow(getActivity());
                    }
                });
            }



            lnrView.addView(view);
        }
    }

    private double amount(){
        databaseHelper = new DatabaseHelper(getActivity());
        myCarts = new ArrayList<>();
        Cursor res = databaseHelper.getAllData();

        while (res.moveToNext()) {
            MyCart rModel = new MyCart();
            rModel.setId(res.getString(0));
            rModel.setPid(res.getString(1));
            rModel.setImage(res.getString(2));
            rModel.setTitle(res.getString(3));
            rModel.setWeight(res.getString(4));
            rModel.setCost(res.getString(5));
            rModel.setQty(res.getString(6));
            rModel.setDiscount(res.getInt(7));
            myCarts.add(rModel);
        }

        double total = 0;
        for (int counter = 0; counter < myCarts.size(); counter++) {
            MyCart cart = myCarts.get(counter);
            if (cart.getDiscount() > 0){
                total += (Double.parseDouble(cart.getCost()) * Double.parseDouble(cart.getQty())) / (100/cart.getDiscount());
            }else{
                total += (Double.parseDouble(cart.getCost()) * Double.parseDouble(cart.getQty()));
            }
        }

        return total;
    }

    private TransactionRequest transactionRequest(){
        TransactionRequest transactionRequest = new TransactionRequest( System.currentTimeMillis() + "", amount());
        transactionRequest.setCustomerDetails(initCustomerDetails());

        databaseHelper = new DatabaseHelper(getActivity());
        myCarts = new ArrayList<>();
        Cursor res = databaseHelper.getAllData();

        while (res.moveToNext()) {
            MyCart rModel = new MyCart();
            rModel.setId(res.getString(0));
            rModel.setPid(res.getString(1));
            rModel.setImage(res.getString(2));
            rModel.setTitle(res.getString(3));
            rModel.setWeight(res.getString(4));
            rModel.setCost(res.getString(5));
            rModel.setQty(res.getString(6));
            rModel.setDiscount(res.getInt(7));
            myCarts.add(rModel);
        }

        ArrayList<ItemDetails> itemDetails = new ArrayList<>();
        for (int counter = 0; counter < myCarts.size(); counter++) {
            MyCart cart = myCarts.get(counter);
            ItemDetails itemDetail = new ItemDetails(cart.getPid(), Integer.parseInt(cart.getCost()), Integer.parseInt(cart.getQty()), cart.getTitle());
            itemDetails.add(itemDetail);
        }

//        ItemDetails itemDetail1 = new ItemDetails("1", 20000, 20, "odading");
//        ItemDetails itemDetail2 = new ItemDetails("2", 50000, 30, "Kelapa");

//        ArrayList<ItemDetails> itemDetails = new ArrayList<>();
//        itemDetails.add(itemDetail1);
//        itemDetails.add(itemDetail2);

        for (int i = 0; i < itemDetails.size(); i++) {
            System.out.println(itemDetails.get(i));
            System.out.println("=======");
        }

        CreditCard creditCard = new CreditCard();

        creditCard.setSaveCard(false);

        creditCard.setAuthentication(Authentication.AUTH_RBA);
        creditCard.setBank(BankType.BCA); //set spesific acquiring bank

        transactionRequest.setCreditCard(creditCard);

        return transactionRequest;
    }

    private CustomerDetails initCustomerDetails() {

        //define customer detail (mandatory for coreflow)
        user = sessionManager.getUserDetails("");
        CustomerDetails mCustomerDetails = new CustomerDetails();

        mCustomerDetails.setPhone(user.getCcode() + user.getMobile());
        mCustomerDetails.setFirstName(user.getName());
        mCustomerDetails.setEmail(user.getEmail());
        return mCustomerDetails;
    }

    @OnClick()
    public void onViewClicked() {
    }

    @OnClick({R.id.img_ldate, R.id.img_rdate})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_ldate:
                minusDate(txtSelectdate.getText().toString());
                break;
            case R.id.img_rdate:
                addDate(txtSelectdate.getText().toString());
                break;
            default:
                break;
        }
    }

    private String getCurrentDate() {
        Date d = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(d);
        try {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, day);  // number of days to add
            formattedDate = df.format(c.getTime());
            c.setTime(df.parse(formattedDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    private String addDate(String dt) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        Date strDate = null;
        try {
            strDate = sdf.parse(dt);
            if ((System.currentTimeMillis() + 432000000) < strDate.getTime()) {
                Log.e("date change ", "--> 1");
                return dt;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        try {

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, day);  // number of days to add
            dt = sdf.format(c.getTime());
            c.setTime(sdf.parse(dt));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        day++;
        txtSelectdate.setText("" + dt);
        return dt;
    }

    private String minusDate(String dt) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date strDate = null;
        try {
            strDate = sdf.parse(dt);
            if ((System.currentTimeMillis() + 86400000) > strDate.getTime()) {
                Log.e("date change ", "--> 1");
                return dt;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        day--;
        try {

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, day);  // number of days to add
            dt = sdf.format(c.getTime());
            c.setTime(sdf.parse(dt));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        txtSelectdate.setText("" + dt);
        return dt;
    }

    @Override
    public void onTransactionFinished(TransactionResult result) {
        if (result.getResponse() != null) {
            switch (result.getStatus()) {
                case TransactionResult.STATUS_SUCCESS:
                    Toast.makeText(getContext(), "Transaction Finished. ID: " + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    Intent intentSuccessPayment = new Intent(getActivity(), SuccessPaymentActivity.class);
                    startActivity(intentSuccessPayment);
                    DatabaseHelper helper = new DatabaseHelper(getActivity());
                    helper.deleteCard();
                    break;
                case TransactionResult.STATUS_PENDING:
                    Toast.makeText(getContext(), "Transaction Pending. ID: " + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    Intent intentPendingPayment = new Intent(getActivity(), PendingPaymentActivity.class);
                    startActivity(intentPendingPayment);
                    break;
                case TransactionResult.STATUS_FAILED:
                    Toast.makeText(getContext(), "Transaction Failed. ID: " + result.getResponse().getTransactionId() + ". Message: " + result.getResponse().getStatusMessage(), Toast.LENGTH_LONG).show();
                    Intent intentFailurePayment = new Intent(getActivity(), FailurePaymentActivity.class);
                    startActivity(intentFailurePayment);
                    break;
            }
            result.getResponse().getValidationMessages();
        } else if (result.isTransactionCanceled()) {
            Toast.makeText(getContext(), "Transaction Canceled", Toast.LENGTH_LONG).show();
        } else {
            if (result.getStatus().equalsIgnoreCase(TransactionResult.STATUS_INVALID)) {
                Toast.makeText(getContext(), "Transaction Invalid", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Transaction Finished with failure.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
