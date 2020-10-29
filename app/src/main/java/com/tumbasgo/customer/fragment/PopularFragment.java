package com.tumbasgo.customer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tumbasgo.customer.R;
import com.tumbasgo.customer.activity.HomeActivity;
import com.tumbasgo.customer.activity.ItemDetailsActivity;
import com.tumbasgo.customer.adepter.ReletedItemAllAdp;
import com.tumbasgo.customer.model.ProductItem;
import com.tumbasgo.customer.model.User;
import com.tumbasgo.customer.utils.SessionManager;
import com.tumbasgo.customer.utils.Utiles;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PopularFragment extends Fragment implements ReletedItemAllAdp.ItemClickListener {
    @BindView(R.id.recycler_view)
    RecyclerView reyCategory;

    SessionManager sessionManager;
    User userData;
    ReletedItemAllAdp itemAdp;
    public PopularFragment() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        ButterKnife.bind(this, view);
        sessionManager = new SessionManager(getActivity());
        userData = sessionManager.getUserDetails("");
        HomeActivity.getInstance().setFrameMargin(0);
        reyCategory.setHasFixedSize(true);
        reyCategory.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        itemAdp = new ReletedItemAllAdp(getActivity(), Utiles.productItems, this);
        reyCategory.setAdapter(itemAdp);
        return view;
    }
    @Override
    public void onItemClick(ProductItem productItem, int position) {
        getActivity().startActivity(new Intent(getActivity(), ItemDetailsActivity.class).putExtra("MyClass", productItem).putParcelableArrayListExtra("MyList", productItem.getPrice()));
    }
    @Override
    public void onResume() {
        super.onResume();
        HomeActivity.getInstance().titleChange();
        HomeActivity.getInstance().setFrameMargin(60);
        if(itemAdp!=null){
            itemAdp.notifyDataSetChanged();
        }
        if (SessionManager.iscart) {
            SessionManager.iscart = false;
            CardFragment fragment = new CardFragment();
            HomeActivity.getInstance().callFragment(fragment);
        }
    }
}
