package com.tumbasgo.customer.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.tumbasgo.customer.R;
import com.tumbasgo.customer.activity.AddressActivity;
import com.tumbasgo.customer.activity.CoupunActivity;
import com.tumbasgo.customer.activity.HomeActivity;
import com.tumbasgo.customer.activity.PaypalActivity;
import com.tumbasgo.customer.activity.RazerpayActivity;
import com.tumbasgo.customer.database.DatabaseHelper;
import com.tumbasgo.customer.database.MyCart;
import com.tumbasgo.customer.model.Address;
import com.tumbasgo.customer.model.AddressData;
import com.tumbasgo.customer.model.PaymentItem;
import com.tumbasgo.customer.model.RestResponse;
import com.tumbasgo.customer.model.User;
import com.tumbasgo.customer.retrofit.APIClient;
import com.tumbasgo.customer.retrofit.GetResult;
import com.tumbasgo.customer.utils.CustPrograssbar;
import com.tumbasgo.customer.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tumbasgo.customer.utils.Utiles;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;


public class OrderSumrryFragment extends Fragment implements GetResult.MyListener {

    @BindView(R.id.my_recycler_view)
    RecyclerView myRecyclerview;
    @BindView(R.id.txt_subtotal)
    TextView txtSubtotal;
    @BindView(R.id.txt_delivery)
    TextView txtDelivery;
    @BindView(R.id.txt_delevritital)
    TextView txtDelevritital;
    @BindView(R.id.txt_total)
    TextView txtTotal;
    @BindView(R.id.btn_cuntinus)
    TextView btnCuntinus;
    @BindView(R.id.lvlone)
    LinearLayout lvlone;
    @BindView(R.id.lvltwo)
    LinearLayout lvltwo;
    @BindView(R.id.txt_changeadress)
    TextView txtChangeadress;
    @BindView(R.id.txt_address)
    TextView txtAddress;
    @BindView(R.id.txt_texo)
    TextView txtTexo;
    @BindView(R.id.txt_tex)
    TextView txtTex;
    @BindView(R.id.img_coopncode)
    ImageView imgCoopncode;
    @BindView(R.id.txt_discount)
    TextView txtDiscount;
    private String time;
    private String data;
    private String payment;
    double total;
    public static int paymentsucsses = 0;
    public static String tragectionID = "0";
    public static boolean isorder = false;
    PaymentItem paymentItem;
    Address selectaddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            time = getArguments().getString("TIME");
            data = getArguments().getString("DATE");
            payment = getArguments().getString("PAYMENT");
            paymentItem = (PaymentItem) getArguments().getSerializable("PAYMENTDETAILS");
        }
    }

    DatabaseHelper databaseHelper;
    List<MyCart> myCarts;
    SessionManager sessionManager;
    Unbinder unbinder;
    User user;
    CustPrograssbar custPrograssbar;
    StaggeredGridLayoutManager gridLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_sumrry, container, false);
        unbinder = ButterKnife.bind(this, view);
        custPrograssbar = new CustPrograssbar();
        databaseHelper = new DatabaseHelper(getActivity());
        sessionManager = new SessionManager(getActivity());
        HomeActivity.getInstance().setFrameMargin(0);
        user = sessionManager.getUserDetails("");
        gridLayoutManager = new StaggeredGridLayoutManager(1, 1);
        myRecyclerview.setLayoutManager(gridLayoutManager);
        getAddress();
        myCarts = new ArrayList<>();
        Cursor res = databaseHelper.getAllData();
        if (res.getCount() == 0) {
            Toast.makeText(getActivity(), "NO DATA FOUND", Toast.LENGTH_LONG).show();
        }
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
        return view;

    }


    private void update(List<MyCart> mData) {

        double[] totalAmount = {0};

        for (int i = 0; i < mData.size(); i++) {
            MyCart cart = mData.get(i);
            double res = (Double.parseDouble(cart.getCost()) / 100.0f) * cart.getDiscount();
            res = Double.parseDouble(cart.getCost()) - res;
            int qrt = databaseHelper.getCard(cart.getPid(), cart.getCost());
            double temp = res * qrt;
            totalAmount[0] = totalAmount[0] + temp;
        }


        txtSubtotal.setText(sessionManager.getStringData(SessionManager.CURRUNCY) + new DecimalFormat("##.##").format(totalAmount[0]));


        double tex = Double.parseDouble(sessionManager.getStringData(SessionManager.tax));
        txtTexo.setText("Service Tax(" + tex + "%)");
        tex = (totalAmount[0] / 100.0f) * tex;
        txtTex.setText(sessionManager.getStringData(SessionManager.CURRUNCY) + new DecimalFormat("##.##").format(tex));
        totalAmount[0] = totalAmount[0] + tex;

        if (payment.equalsIgnoreCase(getResources().getString(R.string.pic_myslf))) {
            txtDelivery.setVisibility(View.VISIBLE);
            txtDelevritital.setVisibility(View.VISIBLE);
            txtDelivery.setText(sessionManager.getStringData(SessionManager.CURRUNCY) + "0");
        } else {
            totalAmount[0] = totalAmount[0] + selectaddress.getDeliveryCharge();
            txtDelivery.setText(sessionManager.getStringData(SessionManager.CURRUNCY) + selectaddress.getDeliveryCharge());
        }
        if (sessionManager.getIntData(SessionManager.COUPON) != 0) {
            imgCoopncode.setImageResource(R.drawable.ic_icons_remove_tag);
        } else {
            imgCoopncode.setImageResource(R.drawable.ic_righta);

        }
        totalAmount[0] = totalAmount[0] - sessionManager.getIntData(SessionManager.COUPON);
        txtTotal.setText(sessionManager.getStringData(SessionManager.CURRUNCY) + new DecimalFormat("##.##").format(totalAmount[0]));
        btnCuntinus.setText("Kirim Pesanan - " + sessionManager.getStringData(SessionManager.CURRUNCY) + new DecimalFormat("##.##").format(totalAmount[0]));
        txtDiscount.setText(sessionManager.getStringData(SessionManager.CURRUNCY) + " " + sessionManager.getIntData(SessionManager.COUPON));

        total = totalAmount[0];
    }


    public class ItemAdp extends RecyclerView.Adapter<ItemAdp.ViewHolder> {
        DatabaseHelper helper = new DatabaseHelper(getActivity());
        private List<MyCart> mData;
        private LayoutInflater mInflater;
        Context mContext;
        SessionManager sessionManager;

        public ItemAdp(Context context, List<MyCart> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
            this.mContext = context;
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            sessionManager = new SessionManager(mContext);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.custome_sumrry, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int i) {
            MyCart cart = mData.get(i);
            Glide.with(getActivity()).load(APIClient.baseUrl + "/" + cart.getImage()).thumbnail(Glide.with(getActivity()).load(R.drawable.lodingimage)).into(holder.imgIcon);
            double res = (Double.parseDouble(cart.getCost()) / 100.0f) * cart.getDiscount();
            res = Double.parseDouble(cart.getCost()) - res;
            holder.txtTitle.setText("" + cart.getTitle());
            MyCart myCart = new MyCart();
            myCart.setPid(cart.getPid());
            myCart.setImage(cart.getImage());
            myCart.setTitle(cart.getTitle());
            myCart.setWeight(cart.getWeight());
            myCart.setCost(cart.getCost());
            int qrt = helper.getCard(myCart.getPid(), myCart.getCost());
            holder.txtPriceanditem.setText(qrt + " item x " + sessionManager.getStringData(SessionManager.CURRUNCY) + new DecimalFormat("##.##").format(res));
            double temp = res * qrt;
            holder.txtPrice.setText(sessionManager.getStringData(SessionManager.CURRUNCY) + new DecimalFormat("##.##").format(temp));

        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.img_icon)
            ImageView imgIcon;
            @BindView(R.id.txt_title)
            TextView txtTitle;
            @BindView(R.id.txt_priceanditem)
            TextView txtPriceanditem;
            @BindView(R.id.txt_price)
            TextView txtPrice;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

        }


    }


    private void orderplace(JSONArray jsonArray) {
        if (selectaddress == null) {
            getAddress();
            return;
        }
        custPrograssbar.prograssCreate(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            jsonObject.put("timesloat", time);
            jsonObject.put("ddate", data);
            jsonObject.put("total", total);
            jsonObject.put("p_method", payment);
            jsonObject.put("address_id", selectaddress.getId());
            jsonObject.put("tax", sessionManager.getStringData(SessionManager.tax));
            jsonObject.put("tid", tragectionID);
            jsonObject.put("cou_amt", sessionManager.getIntData(SessionManager.COUPON));
            jsonObject.put("coupon_id", sessionManager.getIntData(SessionManager.COUPONID));
            jsonObject.put("pname", jsonArray);
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().order((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void callback(JsonObject result, String callNo) {
        try {
            custPrograssbar.closePrograssBar();
            if (callNo.equalsIgnoreCase("1")) {
                Gson gson = new Gson();
                RestResponse response = gson.fromJson(result.toString(), RestResponse.class);
                Toast.makeText(getActivity(), "" + response.getResponseMsg(), Toast.LENGTH_LONG).show();
                if (response.getResult().equals("true")) {
                    lvlone.setVisibility(View.GONE);
                    lvltwo.setVisibility(View.VISIBLE);
                    databaseHelper.deleteCard();
                    isorder = true;

                }
            } else if (callNo.equalsIgnoreCase("2323")) {
                Gson gson = new Gson();
                btnCuntinus.setClickable(true);

                AddressData addressData = gson.fromJson(result.toString(), AddressData.class);
                if (addressData.getResult().equalsIgnoreCase("true")) {
                    if (addressData.getResultData().size() != 0) {
                        selectaddress = addressData.getResultData().get(Utiles.seletAddress);
                        sessionManager.setAddress(SessionManager.address1, selectaddress);
                        if (selectaddress.isUpdateNeed()) {
                            Toast.makeText(getActivity(), "Please Update Your Area Name.Because It's Not match with Our Delivery Location", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getActivity(), AddressActivity.class).putExtra("MyClass", selectaddress));
                        } else {
                            txtAddress.setText(selectaddress.getHno() + "," + selectaddress.getSociety() + "," + selectaddress.getArea() + "," + selectaddress.getLandmark() + "," + selectaddress.getName());
                            ItemAdp itemAdp = new ItemAdp(getActivity(), myCarts);
                            myRecyclerview.setAdapter(itemAdp);
                            update(myCarts);
                        }


                    } else {
                        Toast.makeText(getActivity(), "Tambahkan Alamat Anda ", Toast.LENGTH_LONG).show();

                        AddressFragment fragment = new AddressFragment();
                        HomeActivity.getInstance().callFragment(fragment);
                    }
                } else {
                    Toast.makeText(getActivity(), "Tambahkan Alamat Anda ", Toast.LENGTH_LONG).show();

                    AddressFragment fragment = new AddressFragment();
                    HomeActivity.getInstance().callFragment(fragment);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.txt_changeadress, R.id.btn_cuntinus, R.id.txt_trackorder, R.id.img_coopncode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_changeadress:
                Utiles.isSelect = true;
                AddressFragment fragment = new AddressFragment();
                HomeActivity.getInstance().callFragment(fragment);
                break;
            case R.id.txt_trackorder:
                clearFragment();
                break;
            case R.id.img_coopncode:
                if (sessionManager.getIntData(SessionManager.COUPON) != 0) {
                    sessionManager.setIntData(SessionManager.COUPON, 0);
                    imgCoopncode.setImageResource(R.drawable.ic_righta);
                    update(myCarts);
                    return;
                }
                String PMethod;
                if(payment.equalsIgnoreCase("Razorpay")|| payment.equalsIgnoreCase("Paypal")){
                    PMethod="Pay with online";
                }else {
                    PMethod= payment;

                }
                int temp = (int) Math.round(total);
                startActivity(new Intent(getActivity(), CoupunActivity.class).putExtra("amount", temp).putExtra("payment", PMethod));
                break;
            case R.id.btn_cuntinus:
                btnCuntinus.setClickable(false);
                if (payment.equalsIgnoreCase("Razorpay")) {
                    int temtoal = (int) Math.round(total);
                    startActivity(new Intent(getActivity(), RazerpayActivity.class).putExtra("amount", temtoal).putExtra("detail", paymentItem));
                } else if (payment.equalsIgnoreCase("Paypal")) {
                    startActivity(new Intent(getActivity(), PaypalActivity.class).putExtra("amount", total).putExtra("detail", paymentItem));

                } else if (payment.equalsIgnoreCase("Cash On Delivery") || payment.equalsIgnoreCase("Pickup Myself")) {
                    sendorderServer();
                }

                break;
            default:
                break;
        }
    }

    public void clearFragment() {
        sessionManager = new SessionManager(getActivity());
        User user1 = sessionManager.getUserDetails("");
        HomeActivity.getInstance().titleChange("Hello " + user1.getName());
        MyOrderFragment homeFragment = new MyOrderFragment();
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getFragmentManager().beginTransaction().replace(R.id.fragment_frame, homeFragment).addToBackStack(null).commit();
    }

    private void sendorderServer() {
        Cursor res = databaseHelper.getAllData();
        if (res.getCount() == 0) {
            return;
        }
        if (user.getArea() != null || user.getSociety() != null || user.getHno() != null || user.getMobile() != null) {
            JSONArray jsonArray = new JSONArray();
            while (res.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", res.getString(0));
                    jsonObject.put("pid", res.getString(1));
                    jsonObject.put("image", res.getString(2));
                    jsonObject.put("title", res.getString(3));
                    jsonObject.put("weight", res.getString(4));
                    jsonObject.put("cost", res.getString(5));
                    jsonObject.put("qty", res.getString(6));
                    jsonArray.put(jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            orderplace(jsonArray);
        } else {
            startActivity(new Intent(getActivity(), AddressActivity.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        HomeActivity.getInstance().serchviewHide();
        HomeActivity.getInstance().setFrameMargin(0);
        try {
            if (btnCuntinus != null) {
                btnCuntinus.setClickable(true);
            }
            if (paymentsucsses == 1) {
                paymentsucsses = 0;
                sendorderServer();
            }
            if (sessionManager != null) {
                selectaddress = sessionManager.getAddress(SessionManager.address1);
                if (selectaddress != null) {
                    txtAddress.setText(selectaddress.getHno() + "," + selectaddress.getSociety() + "," + selectaddress.getArea() + "," + selectaddress.getLandmark() + "," + selectaddress.getName());
                    update(myCarts);
                    if (Utiles.isRef) {
                        Utiles.isRef = false;
                        ItemAdp itemAdp = new ItemAdp(getActivity(), myCarts);
                        myRecyclerview.setAdapter(itemAdp);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAddress() {
        custPrograssbar.prograssCreate(getActivity());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", user.getId());
            JsonParser jsonParser = new JsonParser();
            Call<JsonObject> call = APIClient.getInterface().getAddress((JsonObject) jsonParser.parse(jsonObject.toString()));
            GetResult getResult = new GetResult();
            getResult.setMyListener(this);
            getResult.callForLogin(call, "2323");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
