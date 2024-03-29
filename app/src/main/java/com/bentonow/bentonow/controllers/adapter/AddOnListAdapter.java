/**
 *
 */
package com.bentonow.bentonow.controllers.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.listener.ListenerAddOn;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.ui.wrapper.AddOnWrapper;

import java.util.ArrayList;

/**
 * @author José Torres Fuentes
 */

public class AddOnListAdapter extends RecyclerView.Adapter<AddOnWrapper> {

    public static final String TAG = "AddOnListAdapter";
    public ArrayList<DishModel> aListDish = new ArrayList<>();
    private Activity mActivity;
    private ListenerAddOn mListener;
    private int iDishSelected = -1;
    private DishDao mDishDao = new DishDao();
    private boolean bIsMenuOD;

    /**
     * @param context
     */
    public AddOnListAdapter(Activity context, boolean bIsMenuOD, ListenerAddOn mListener) {
        this.mActivity = context;
        this.mListener = mListener;
        this.bIsMenuOD = bIsMenuOD;
    }


    @Override
    public AddOnWrapper onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_add_on, parent, false);
        AddOnWrapper vh = new AddOnWrapper(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(AddOnWrapper viewHolder, final int position) {
        final DishModel mDish = aListDish.get(position);

        viewHolder.getTxtTitle().setText(mDish.name);
        viewHolder.getTxtPrice().setText(String.format(mActivity.getString(R.string.money_format), mDish.price));
        viewHolder.getTxtDescription().setText(mDish.description);

        if (viewHolder.getImgDish().getTag() == null || !viewHolder.getImgDish().getTag().equals(mDish.image1)) {
            ImageUtils.initImageLoader().displayImage(mDish.image1, viewHolder.getImgDish(), ImageUtils.dishMainImageOptions());
            viewHolder.getImgDish().setTag(mDish.image1);
        }

        viewHolder.getImgDish().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    iDishSelected = position;
                    notifyDataSetChanged();
                    DebugUtils.logDebug("onDishClick()", position);
                }
            }
        });

        viewHolder.getImgAddDish().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onAddClick(position);
                }
            }
        });


        viewHolder.getImgRemoveDish().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onRemoveClick(position);
                }
            }
        });

        viewHolder.getTxtNumberAddOn().setText(String.valueOf(mDishDao.countItemsById(mDish.itemId)));
        viewHolder.getImgGradient().setVisibility(iDishSelected == position ? View.VISIBLE : View.INVISIBLE);

        if (mDish.is_oa_only == 1) {
            viewHolder.getImgAddDish().setImageResource(R.drawable.vector_add_gray);
            viewHolder.getImgRemoveDish().setImageResource(R.drawable.vector_remove_gray);
            viewHolder.getImgAddDish().setOnClickListener(null);
            viewHolder.getImgRemoveDish().setOnClickListener(null);
            viewHolder.getImgSoldOut().setVisibility(View.GONE);
        } else if (!mDishDao.canBeAdded(mDish) || mDishDao.isSoldOut(mDish, true, bIsMenuOD)) {
            viewHolder.getImgSoldOut().setVisibility(View.VISIBLE);
            viewHolder.getImgGradient().setVisibility(View.VISIBLE);

            viewHolder.getImgAddDish().setImageResource(R.drawable.vector_add_gray);
            viewHolder.getImgAddDish().setOnClickListener(null);

            if (mDishDao.countItemsById(mDish.itemId) == 0) {
                viewHolder.getImgRemoveDish().setImageResource(R.drawable.vector_remove_gray);
                viewHolder.getImgRemoveDish().setOnClickListener(null);
            } else {
                viewHolder.getImgRemoveDish().setImageResource(R.drawable.vector_remove_green);
            }

        } else {
            viewHolder.getImgSoldOut().setVisibility(View.INVISIBLE);
            viewHolder.getImgAddDish().setImageResource(R.drawable.vector_add_green);
            viewHolder.getImgRemoveDish().setImageResource(R.drawable.vector_remove_green);
        }

        viewHolder.getTxtDescription().setVisibility(iDishSelected == position ? View.VISIBLE : View.INVISIBLE);
        viewHolder.getTxtOaLabel().setVisibility(mDish.is_oa_only == 0 || iDishSelected == position ? View.GONE : View.VISIBLE);

    }

    @Override
    public int getItemCount() {
        return aListDish.size();
    }

}
