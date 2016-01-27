package com.bentonow.bentonow.controllers.order;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.adapter.SpinnerOADayListAdapter;
import com.bentonow.bentonow.controllers.adapter.SpinnerOATimeListAdapter;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.dao.SettingsDao;
import com.bentonow.bentonow.dao.StockDao;
import com.bentonow.bentonow.listener.InterfaceCustomerService;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.menu.TimesModel;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.service.BentoCustomerService;
import com.bentonow.bentonow.ui.AutoFitTxtView;
import com.bentonow.bentonow.ui.BackendAutoFitTextView;
import com.bentonow.bentonow.ui.BackendTextView;
import com.bentonow.bentonow.ui.material.ButtonFlat;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.mixpanel.android.mpmetrics.Tweak;

import org.json.JSONObject;

import java.util.ArrayList;

public class BuildBentoActivity extends BaseFragmentActivity implements View.OnClickListener, InterfaceCustomerService {

    static final String TAG = "BuildBentoActivity";

    private BackendTextView txtAddMain;
    private BackendTextView txtAddSide1;
    private BackendTextView txtAddSide2;
    private BackendTextView txtAddSide3;
    private BackendTextView txtAddSide4;

    private BackendAutoFitTextView btnContinue;
    private BackendAutoFitTextView btnAddAnotherBento;
    private TextView txtNumBento;
    private LinearLayout layoutAddOns;
    private LinearLayout wrapperOd;
    private LinearLayout wrapperOa;


    private int orderIndex;
    private AutoFitTxtView txtPromoName;
    private AutoFitTxtView txtEta;
    private AutoFitTxtView btnAddOn;
    private AutoFitTxtView txtDateTimeToolbar;
    private AutoFitTxtView txtOdDescription;
    private AutoFitTxtView txtOaHeader;
    private AutoFitTxtView txtOdHeader;
    private ImageView actionbarLeftBtn;
    private ImageView actionbarRightBtn;

    private ImageView imgMain;
    private ImageView imgMain4;
    private ImageView imgSide1;
    private ImageView imgSide14;
    private ImageView imgSide2;
    private ImageView imgSide24;
    private ImageView imgSide3;
    private ImageView imgSide34;
    private ImageView imgSide4;
    private ImageView imgMainSoldOut;
    private ImageView imgSide1SoldOut;
    private ImageView imgSide14SoldOut;
    private ImageView imgSide2SoldOut;
    private ImageView imgSide24SoldOut;
    private ImageView imgSide3SoldOut;
    private ImageView imgSide34SoldOut;
    private ImageView imgSide4SoldOut;
    private ImageView imgMainSoldOut4;
    private ImageView imgDropDownUp;
    private ImageView imgDividerStatus;
    private RelativeLayout containerMainTitle;
    private RelativeLayout containerMainTitle4;
    private RelativeLayout containerSide1Title;
    private RelativeLayout containerSide14Title;
    private RelativeLayout containerSide2Title;
    private RelativeLayout containerSide24Title;
    private RelativeLayout containerSide3Title;
    private RelativeLayout containerSide34Title;
    private RelativeLayout containerSide4Title;
    private RelativeLayout layoutPodFive;
    private RelativeLayout layoutPodFour;
    private RelativeLayout containerDateTime;
    private FrameLayout layoutDateTime;
    private FrameLayout containerCancelWidget;
    private LinearLayout containerDateTimeSelection;
    private LinearLayout containerSpinnerDayTimeOa;

    private TextView txtTitleMain;
    private TextView txtTitleMain4;
    private TextView txtTitleSide1;
    private TextView txtTitleSide14;
    private TextView txtTitleSide2;
    private TextView txtTitleSide24;
    private TextView txtTitleSide3;
    private TextView txtTitleSide34;
    private TextView txtTitleSide4;
    private TextView txtOdTitle;

    private Spinner spinnerDate;
    private Spinner spinnerTime;

    private ImageView checkBoxOD;
    private ImageView checkBoxOA;

    private ButtonFlat buttonCancel;
    private ButtonFlat buttonAccept;

    private SpinnerOATimeListAdapter mSpinnerTimeAdapter;
    private SpinnerOADayListAdapter mSpinnerDayAdapter;

    private ConfirmationDialog mDialog;

    private Menu mMenu;
    private Menu mOAPreselectedMenu;
    private Order mOrder;
    private ArrayList<String> aMenusId = new ArrayList<>();
    private CountDownTimer mCountDown;

    private String sContinueBtn;
    private long lMilliSecondsRemaining;
    private boolean bSawAddOns = false;
    private boolean bHasAddOns = false;
    private boolean bShowDateTime = false;
    private boolean bIsMenuOD = false;
    private boolean bIsMenuAlreadySelected = false;
    private boolean bIsAsapChecked = false;
    //private static Tweak<Boolean> showBanner = MixpanelAPI.booleanTweak("Show Banner", false);
    private static Tweak<Boolean> showAddons = MixpanelAPI.booleanTweak("Show AddOns", false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_build_bento);

        getBtnContinue().setOnClickListener(this);
        getActionbarLeftBtn().setOnClickListener(this);
        getActionbarRightBtn().setOnClickListener(this);
        getLayoutDateTime().setOnClickListener(this);
        getButtonCancel().setOnClickListener(this);
        getButtonAccept().setOnClickListener(this);
        getContainerCancelWidget().setOnClickListener(this);
        getCheckBoxOD().setOnClickListener(this);
        getCheckBoxOA().setOnClickListener(this);

        getTxtEta().setText(String.format(getString(R.string.build_bento_eta), MenuDao.eta_min + "-" + MenuDao.eta_max));

        getSpinnerDate().setAdapter(getSpinnerDayAdapter());
        getSpinnerTime().setAdapter(getSpinnerTimeAdapter());

        sContinueBtn = IosCopyDao.get("build-title");

        updateWidget();

    }

    @Override
    protected void onResume() {
        if (mMenu == null) {
            //openErrorActivity();
        } else {
            createOrder();
        }

        updateUI();


        super.onResume();
    }

    private void createOrder() {
        mOrder = mOrderDao.getCurrentOrder();

        if (mOrder == null) {
            mOrder = mOrderDao.getNewOrder(bIsMenuOD);
            mOrder.MealName = mMenu.meal_name;
            mOrder.MenuType = mMenu.menu_type;
            mOrder.MenuId = mMenu.menu_id;
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.MEAL_NAME, mMenu.meal_name);
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.POD_MODE, SettingsDao.getCurrent().pod_mode);

            mOrderDao.updateOrder(mOrder);
        }

        orderIndex = mOrder.currentOrderItem;

        if (mOrder.OrderItems.size() == 0) {
            mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.CUSTOM_BENTO_BOX));
            mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.ADD_ON));
        } else if (mOrder.OrderItems.size() <= orderIndex) {
            orderIndex = mOrder.OrderItems.size() - 1;
        }

        for (int a = 0; a < mMenu.dishModels.size(); a++) {
            if (mMenu.dishModels.get(a).type.equals("addon")) {
                bHasAddOns = true;
                break;
            }
        }
    }

    public void updateUI() {
        updateDishUI();
        getButtonCancel().setVisibility(bIsMenuAlreadySelected ? View.VISIBLE : View.GONE);
        getTxtEta().setVisibility(bIsMenuOD ? View.VISIBLE : View.GONE);
        getImgDividerStatus().setVisibility(bIsMenuOD ? View.VISIBLE : View.GONE);
        getTxtPromoName().setText(String.format(getString(R.string.build_bento_price), BentoNowUtils.getDefaultPriceBento(DishDao.getLowestMainPrice(mMenu))));
        getTxtOdHeader().setTextColor(getResources().getColor(bIsMenuOD ? R.color.primary : R.color.black));
        getTxtOaHeader().setTextColor(getResources().getColor(bIsMenuOD ? R.color.black : R.color.primary));

        if (!bIsMenuOD && bIsMenuAlreadySelected && mMenu.menu_id.equals(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.get(0).menu_id))
            setOAHashTimer(BentoNowUtils.showOATimer(mMenu));
        else if (mCountDown != null) {
            mCountDown.cancel();
            mCountDown = null;
        }

        if (mBentoDao.isBentoComplete(mOrder.OrderItems.get(orderIndex)) && !StockDao.isSold()) {
            getBtnContinue().setBackgroundColor(getResources().getColor(R.color.btn_green));
            getBtnContinue().setText(IosCopyDao.get("build-button-2"));
            sContinueBtn = IosCopyDao.get("build-button-2");
            getBtnAddAnotherBento().setTextColor(getResources().getColor(R.color.btn_green));
            getBtnAddAnotherBento().setOnClickListener(this);
            getBtnAddOn().setTextColor(getResources().getColor(R.color.btn_green));
            getBtnAddOn().setOnClickListener(this);
        } else {
            getBtnContinue().setBackgroundColor(getResources().getColor(R.color.gray));
            getBtnContinue().setText(IosCopyDao.get("build-button-1"));
            sContinueBtn = IosCopyDao.get("build-title");
            getBtnAddAnotherBento().setTextColor(getResources().getColor(R.color.btn_green_trans));
            getBtnAddAnotherBento().setOnClickListener(null);
            getBtnAddOn().setTextColor(getResources().getColor(R.color.btn_green_trans));
            getBtnAddOn().setOnClickListener(null);
        }

        if (mOrderDao.countCompletedOrders(mOrder) == 0) {
            getTxtNumBento().setVisibility(View.GONE);
            getTxtNumBento().setText("0");
            getActionbarRightBtn().setImageResource(R.drawable.ic_ab_bento);
        } else {
            getTxtNumBento().setVisibility(View.VISIBLE);
            getTxtNumBento().setText(mOrderDao.countCompletedOrders(mOrder) + "");
            getActionbarRightBtn().setImageResource(R.drawable.ic_ab_bento_completed);
        }


        getBtnAddOn().setVisibility(bHasAddOns ? View.VISIBLE : View.GONE);
    }

    private void updateWidget() {
        aMenusId = MenuDao.getCurrentMenuIds();
        getSpinnerDayAdapter().clear();
        getSpinnerTimeAdapter().clear();

        if (MenuDao.gateKeeper.getAppOnDemandWidget() != null) {
            getWrapperOd().setVisibility(View.VISIBLE);
            getTxtOdTitle().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getTitle());
            getTxtOdDescription().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getText());
        } else
            getWrapperOd().setVisibility(View.GONE);

        if (MenuDao.gateKeeper.getAvailableServices() != null && MenuDao.gateKeeper.getAvailableServices().mOrderAhead != null) {
            getWrapperOa().setVisibility(View.VISIBLE);
            getSpinnerDayAdapter().addAll(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus);
            getSpinnerDayAdapter().notifyDataSetChanged();
        } else
            getWrapperOa().setVisibility(View.GONE);

        if (MenuDao.gateKeeper.isOnDemand() && MenuDao.gateKeeper.getAppOnDemandWidget() != null && MenuDao.gateKeeper.getAppOnDemandWidget().isSelected() && MenuDao.get() != null) {
            bIsMenuOD = true;
            mMenu = MenuDao.get();
            createOrder();
            getTxtDateTimeToolbar().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getTitle());
        } else {
            bIsMenuOD = false;
            mMenu = MenuDao.cloneMenu(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.get(0));
            createOrder();
            updateDayOASpinner(mMenu, 0);
            mOrder.for_date = mMenu.for_date;
            updateTimeOrder(mMenu.listTimeModel.get(0));
            getTxtDateTimeToolbar().setText(BentoNowUtils.getDayTimeSelected(mOrder));
        }

        bIsAsapChecked = bIsMenuOD ? true : false;
        updateWidgetSelection();

        getSpinnerDate().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateDayOASpinner(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.get(i), 0);
                getSpinnerDayAdapter().iSelectedPosition = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        getSpinnerTime().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int iPosition, long l) {
                for (int a = 0; a < mOAPreselectedMenu.listTimeModel.size(); a++) {
                    if (a == iPosition) {
                        mOAPreselectedMenu.listTimeModel.get(a).isSelected = true;
                    } else
                        mOAPreselectedMenu.listTimeModel.get(a).isSelected = false;
                }
                getSpinnerTimeAdapter().iSelectedPosition = iPosition;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        setDateTime(true, false);
    }

    private void updateDayOASpinner(Menu mNewMenu, final int iTimePosition) {
        mOAPreselectedMenu = MenuDao.cloneMenu(mNewMenu);

        for (int a = 0; a < mOAPreselectedMenu.listTimeModel.size(); a++)
            mOAPreselectedMenu.listTimeModel.get(a).isSelected = a == iTimePosition;

        getSpinnerTimeAdapter().clear();
        getSpinnerTimeAdapter().addAll(mOAPreselectedMenu.listTimeModel);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSpinnerTimeAdapter().notifyDataSetChanged();
                getSpinnerTime().setSelection(iTimePosition, true);
            }
        });
    }

    private void updateTimeOrder(TimesModel mTimeModel) {
        mOrder.scheduled_window_start = mTimeModel.start;
        mOrder.scheduled_window_end = mTimeModel.end;
        mOrder.OrderDetails.delivery_price = BentoNowUtils.getDeliveryPriceByTime(mTimeModel);
        DebugUtils.logDebug(TAG, "On Update Time : " + mOrder.scheduled_window_start + " - " + mOrder.scheduled_window_end);
        mOrderDao.updateOrder(mOrder);
    }


    private void restartWidget() {
        if (bIsMenuAlreadySelected)
            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  bIsAsapChecked = bIsMenuOD ? true : false;
                                  updateWidgetSelection();

                                  if (!bIsMenuOD) {
                                      for (int a = 0; a < MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.size(); a++) {
                                          if (MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.get(a).menu_id.equals(mMenu.menu_id)) {
                                              getSpinnerDate().setSelection(a, false);

                                              for (int b = 0; b < mOAPreselectedMenu.listTimeModel.size(); b++) {
                                                  if (mOAPreselectedMenu.listTimeModel.get(b).start.equals(mOrder.scheduled_window_start)) {
                                                      updateDayOASpinner(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.get(a), b);
                                                  }
                                              }

                                              getTxtDateTimeToolbar().setText(BentoNowUtils.getDayTimeSelected(mOrder));
                                              break;
                                          }
                                      }
                                  }
                              }
                          }

            );
    }

    private void updateDishUI() {
        OrderItem item = mOrder.OrderItems.get(orderIndex);

        if (SettingsDao.getCurrent().pod_mode.equals("5")) {
            getLayoutPodFive().setVisibility(View.VISIBLE);
            getLayoutPodFour().setVisibility(View.GONE);

            if (item.items.get(0) == null || item.items.get(0).name.isEmpty()) {
                getImgMain().setImageBitmap(null);
                getTxtTitleMain().setText("");
                getTxtAddMain().setVisibility(View.VISIBLE);
                getContainerMainTitle().setVisibility(View.GONE);
                getImgMainSoldOut().setVisibility(View.GONE);
            } else {
                if (getImgMain().getTag() == null || !getImgMain().getTag().equals(item.items.get(0).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(0).image1, getImgMain(), ImageUtils.dishMainImageOptions());
                    getImgMain().setTag(item.items.get(0).image1);
                }
                getTxtTitleMain().setText(item.items.get(0).name);
                getTxtAddMain().setVisibility(View.GONE);
                getContainerMainTitle().setVisibility(View.VISIBLE);
                getImgMainSoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(0), false, bIsMenuOD) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(1) == null || item.items.get(1).name.isEmpty()) {
                getImgSide1().setImageBitmap(null);
                getTxtTitleSide1().setText("");
                getTxtAddSide1().setVisibility(View.VISIBLE);
                getContainerSide1Title().setVisibility(View.GONE);
                getImgSide1SoldOut().setVisibility(View.GONE);
            } else {
                if (getImgSide1().getTag() == null || !getImgSide1().getTag().equals(item.items.get(1).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(1).image1, getImgSide1(), ImageUtils.dishSideImageOptions());
                    getImgSide1().setTag(item.items.get(1).image1);
                }
                getTxtTitleSide1().setText(item.items.get(1).name);
                getTxtAddSide1().setVisibility(View.GONE);
                getContainerSide1Title().setVisibility(View.VISIBLE);
                getImgSide1SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(1), false, bIsMenuOD) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(2) == null || item.items.get(2).name.isEmpty()) {
                getImgSide2().setImageBitmap(null);
                getTxtTitleSide2().setText("");
                getTxtAddSide2().setVisibility(View.VISIBLE);
                getContainerSide2Title().setVisibility(View.GONE);
                getImgSide2SoldOut().setVisibility(View.GONE);
            } else {
                if (getImgSide2().getTag() == null || !getImgSide2().getTag().equals(item.items.get(2).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(2).image1, getImgSide2(), ImageUtils.dishSideImageOptions());
                    getImgSide2().setTag(item.items.get(2).image1);
                }
                getTxtAddSide2().setVisibility(View.GONE);
                getTxtTitleSide2().setText(item.items.get(2).name);
                getContainerSide2Title().setVisibility(View.VISIBLE);
                getImgSide2SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(2), false, bIsMenuOD) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(3) == null || item.items.get(3).name.isEmpty()) {
                getImgSide3().setImageBitmap(null);
                getTxtTitleSide3().setText("");
                getTxtAddSide3().setVisibility(View.VISIBLE);
                getContainerSide3Title().setVisibility(View.GONE);
                getImgSide3SoldOut().setVisibility(View.GONE);
            } else {
                if (getImgSide3().getTag() == null || !getImgSide3().getTag().equals(item.items.get(3).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(3).image1, getImgSide3(), ImageUtils.dishSideImageOptions());
                    getImgSide3().setTag(item.items.get(3).image1);
                }
                getTxtTitleSide3().setText(item.items.get(3).name);
                getTxtAddSide3().setVisibility(View.GONE);
                getContainerSide3Title().setVisibility(View.VISIBLE);
                getImgSide3SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(3), false, bIsMenuOD) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(4) == null || item.items.get(4).name.isEmpty()) {
                getImgSide4().setImageBitmap(null);
                getTxtTitleSide4().setText("");
                getTxtAddSide4().setVisibility(View.VISIBLE);
                getContainerSide4Title().setVisibility(View.GONE);
                getImgSide4SoldOut().setVisibility(View.GONE);
            } else {
                if (getImgSide4().getTag() == null || !getImgSide4().getTag().equals(item.items.get(4).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(4).image1, getImgSide4(), ImageUtils.dishSideImageOptions());
                    getImgSide4().setTag(item.items.get(3).image1);
                }
                getTxtAddSide4().setVisibility(View.GONE);
                getTxtTitleSide4().setText(item.items.get(4).name);
                getContainerSide4Title().setVisibility(View.VISIBLE);
                getImgSide4SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(4), false, bIsMenuOD) ? View.VISIBLE : View.GONE);
            }
        } else {
            getLayoutPodFive().setVisibility(View.GONE);
            getLayoutPodFour().setVisibility(View.VISIBLE);

            if (item.items.get(0) == null || item.items.get(0).name.isEmpty()) {
                getImgMain4().setImageBitmap(null);
                getTxtTitleMain4().setText("");
                getContainerMainTitle4().setVisibility(View.GONE);
                getImgMainSoldOut4().setVisibility(View.GONE);
            } else {
                if (getImgMain4().getTag() == null || !getImgMain4().getTag().equals(item.items.get(0).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(0).image1, getImgMain4(), ImageUtils.dishMainImageOptions());
                    getImgMain4().setTag(item.items.get(0).image1);
                }
                getTxtTitleMain4().setText(item.items.get(0).name);
                getContainerMainTitle4().setVisibility(View.VISIBLE);
                getImgMainSoldOut4().setVisibility(mDishDao.isSoldOut(item.items.get(0), false, bIsMenuOD) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(1) == null || item.items.get(1).name.isEmpty()) {
                getImgSide14().setImageBitmap(null);
                getTxtTitleSide14().setText("");
                getContainerSide14Title().setVisibility(View.GONE);
                getImgSide14SoldOut().setVisibility(View.GONE);
            } else {
                if (getImgSide14().getTag() == null || !getImgSide14().getTag().equals(item.items.get(1).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(1).image1, getImgSide14(), ImageUtils.dishSideImageOptions());
                    getImgSide14().setTag(item.items.get(1).image1);
                }
                getTxtTitleSide14().setText(item.items.get(1).name);
                getContainerSide14Title().setVisibility(View.VISIBLE);
                getImgSide14SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(1), false, bIsMenuOD) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(2) == null || item.items.get(2).name.isEmpty()) {
                getImgSide24().setImageBitmap(null);
                getTxtTitleSide24().setText("");
                getContainerSide24Title().setVisibility(View.GONE);
                getImgSide24SoldOut().setVisibility(View.GONE);
            } else {
                if (getImgSide24().getTag() == null || !getImgSide24().getTag().equals(item.items.get(2).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(2).image1, getImgSide24(), ImageUtils.dishSideImageOptions());
                    getImgSide24().setTag(item.items.get(2).image1);
                }
                getTxtTitleSide24().setText(item.items.get(2).name);
                getContainerSide24Title().setVisibility(View.VISIBLE);
                getImgSide24SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(2), false, bIsMenuOD) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(3) == null || item.items.get(3).name.isEmpty()) {
                getImgSide34().setImageBitmap(null);
                getTxtTitleSide34().setText("");
                getContainerSide34Title().setVisibility(View.GONE);
                getImgSide34SoldOut().setVisibility(View.GONE);
            } else {
                if (getImgSide34().getTag() == null || !getImgSide34().getTag().equals(item.items.get(3).image1)) {
                    ImageUtils.initImageLoader().displayImage(item.items.get(3).image1, getImgSide34(), ImageUtils.dishSideImageOptions());
                    getImgSide34().setTag(item.items.get(3).image1);
                }
                getTxtTitleSide34().setText(item.items.get(3).name);
                getContainerSide34Title().setVisibility(View.VISIBLE);
                getImgSide34SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(3), false, bIsMenuOD) ? View.VISIBLE : View.GONE);
            }
        }

    }

    void autocomplete() {
        if (mOrder.OrderItems.get(orderIndex).items.get(0).name.isEmpty()) {
            mOrder.OrderItems.get(orderIndex).items.set(0, mDishDao.updateDishItem(mOrder.OrderItems.get(orderIndex).items.get(0), mDishDao.getFirstAvailable(mMenu, "main", null, bIsMenuOD)));
        }
        int iNumDish = mOrder.OrderItems.get(orderIndex).items.size();

        int[] ids = new int[iNumDish - 1];

        for (int i = 1; i < iNumDish; ++i) {
            if (mOrder.OrderItems.get(orderIndex).items.get(i).name.isEmpty()) {
                DishModel dishModel = mDishDao.getFirstAvailable(mMenu, "side", ids, bIsMenuOD);

                if (dishModel == null)
                    continue;

                ids[i - 1] = dishModel.itemId;

                dishModel = DishDao.clone(dishModel);
                dishModel.type += i;

                mOrder.OrderItems.get(orderIndex).items.set(i, mDishDao.updateDishItem(mOrder.OrderItems.get(orderIndex).items.get(i), dishModel));
            }

        }
        updateUI();
    }

    private void openAddOnsActivity() {
        Intent mAddOnActivity = new Intent(BuildBentoActivity.this, AddOnActivity.class);
        mAddOnActivity.putExtra(AddOnActivity.TAG_OPEN_BY, ConstantUtils.optOpenAddOn.BUILDER);
        mAddOnActivity.putExtra(Menu.TAG, mMenu);
        bSawAddOns = true;
        startActivity(mAddOnActivity);
        overridePendingTransition(R.anim.bottom_slide_in, R.anim.none);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_left_btn:
                BentoNowUtils.openSettingsActivity(BuildBentoActivity.this);
                break;
            case R.id.actionbar_right_btn:
                if (!bShowDateTime)
                    if (mBentoDao.isBentoComplete(mOrder.OrderItems.get(orderIndex))) {
                        onContinueOrderPressed();
                    } else {
                        mDialog = new ConfirmationDialog(BuildBentoActivity.this, null, IosCopyDao.get("build-not-complete-text"));
                        mDialog.addAcceptButton(IosCopyDao.get("build-not-complete-confirmation-2"), BuildBentoActivity.this);
                        mDialog.addCancelButton(IosCopyDao.get("build-not-complete-confirmation-1"), BuildBentoActivity.this);

                        mDialog.show();
                    }
                break;
            case R.id.button_accept:
                if (!bShowDateTime)
                    autocomplete();
                break;
            case R.id.btn_continue:
                if (!bShowDateTime)
                    onContinueOrderPressed();
                break;
            case R.id.btn_add_on_add_on:
                if (!bShowDateTime)
                    openAddOnsActivity();
                break;
            case R.id.btn_add_another_bento:
                if (!bShowDateTime)
                    onAddAnotherBentoPressed();
                break;
            case R.id.layout_date_time:
                setDateTime(true, true);
                restartWidget();
                break;
            case R.id.button_accept_widget:
                boolean bChangeMenu = false;
                if (!bIsMenuAlreadySelected) {
                    bIsMenuAlreadySelected = true;
                    forceChangeMenu();
                    setDateTime(true, true);
                } else {
                    if (bIsMenuOD) {
                        if (!bIsAsapChecked) {
                            bChangeMenu = true;
                        }
                    } else {
                        if (bIsAsapChecked) {
                            bChangeMenu = true;
                        } else {
                            if (!mMenu.menu_id.equals(mOAPreselectedMenu.menu_id)) {
                                bChangeMenu = true;
                            } else {
                                for (int a = 0; a < mOAPreselectedMenu.listTimeModel.size(); a++)
                                    if (mOAPreselectedMenu.listTimeModel.get(a).isSelected)
                                        updateTimeOrder(mOAPreselectedMenu.listTimeModel.get(a));
                            }
                        }
                    }

                    if (bChangeMenu) {
                        mDialog = new ConfirmationDialog(BuildBentoActivity.this, "Change Menu", "Are you sure you want to change your menu? you have to build new bentos again");
                        mDialog.addAcceptButton(IosCopyDao.get("build-not-complete-confirmation-2"), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                forceChangeMenu();
                            }
                        });
                        mDialog.addCancelButton(IosCopyDao.get("build-not-complete-confirmation-1"), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        restartWidget();
                                    }
                                }

                        );
                        mDialog.show();
                    } else
                        restartWidget();

                    setDateTime(!bChangeMenu, true);
                }
                break;
            case R.id.button_cancel_widget:
                setDateTime(true, true);
                restartWidget();
                break;
            case R.id.container_cancel_widget:
                setDateTime(true, true);
                restartWidget();
                break;
            case R.id.checkbox_od:
                if (!bIsAsapChecked) {
                    bIsAsapChecked = true;
                    updateWidgetSelection();
                }
                break;
            case R.id.checkbox_oa:
                if (bIsAsapChecked) {
                    bIsAsapChecked = false;
                    updateWidgetSelection();
                }
                break;
            default:
                DebugUtils.logError(TAG, String.valueOf(v.getId()));
                break;
        }
    }

    public void onAddMainPressed(View view) {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectMainCustomActivity.class);
            intent.putExtra(Menu.TAG, mMenu);
            startActivity(intent);
        }
    }

    public void onAddSide1Pressed(View view) {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectSideCustomActivity.class);
            intent.putExtra(Menu.TAG, mMenu);
            intent.putExtra("itemIndex", 1);
            startActivity(intent);
        }
    }

    public void onAddSide2Pressed(View view) {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectSideCustomActivity.class);
            intent.putExtra(Menu.TAG, mMenu);
            intent.putExtra("itemIndex", 2);
            startActivity(intent);
        }
    }

    public void onAddSide3Pressed(View view) {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectSideCustomActivity.class);
            intent.putExtra(Menu.TAG, mMenu);
            intent.putExtra("itemIndex", 3);
            startActivity(intent);
        }
    }

    public void onAddSide4Pressed(View view) {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectSideCustomActivity.class);
            intent.putExtra(Menu.TAG, mMenu);
            intent.putExtra("itemIndex", 4);
            startActivity(intent);
        }
    }

    public void onAddAnotherBentoPressed() {
        if (SettingsDao.getCurrent().pod_mode.equals("5")) {
            getImgMain().setTag(null);
            getImgSide1().setTag(null);
            getImgSide2().setTag(null);
            getImgSide3().setTag(null);
            getImgSide4().setTag(null);
        } else {
            getImgMain4().setTag(null);
            getImgSide14().setTag(null);
            getImgSide24().setTag(null);
            getImgSide34().setTag(null);
        }


        mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.CUSTOM_BENTO_BOX));
        mOrder.currentOrderItem = orderIndex = mOrder.OrderItems.size() - 1;

        mOrderDao.updateOrder(mOrder);

        updateUI();
    }

    public void onContinueOrderPressed() {
        String sSoldOutItems = mOrderDao.calculateSoldOutItems(mOrder, bIsMenuOD);
        mOrderDao.updateOrder(mOrder);

        if (!mBentoDao.isBentoComplete(mOrder.OrderItems.get(orderIndex))) {

            for (int a = 0; a < mOrder.OrderItems.get(orderIndex).items.size(); a++) {

                if (mOrder.OrderItems.get(orderIndex).items.get(a) == null || mOrder.OrderItems.get(orderIndex).items.get(a).name.isEmpty()) {
                    if (a == 0) {
                        Intent intent = new Intent(this, SelectMainCustomActivity.class);
                        intent.putExtra(Menu.TAG, mMenu);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, SelectSideCustomActivity.class);
                        intent.putExtra(Menu.TAG, mMenu);
                        intent.putExtra("itemIndex", a);
                        startActivity(intent);
                    }
                    break;
                }
            }
        } else if (!sSoldOutItems.isEmpty()) {
            updateUI();
            WidgetsUtils.createShortToast(String.format(getString(R.string.error_sold_out_items), sSoldOutItems));
            // if (bHasAddOns && showAddons.get() && !bSawAddOns) {
        } else if (bHasAddOns && !bSawAddOns) {
            openAddOnsActivity();
        } else if (BentoNowUtils.isValidCompleteOrder(BuildBentoActivity.this)) {
            track();
            BentoNowUtils.openCompleteOrderActivity(BuildBentoActivity.this);
        }
    }

    private void forceChangeMenu() {
        DebugUtils.logDebug(TAG, "The menu has to be Changed");
        bIsMenuOD = bIsAsapChecked ? true : false;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMenu = bIsMenuOD ? MenuDao.get() : MenuDao.cloneMenu(mOAPreselectedMenu);

                mOrderDao.cleanUp();
                createOrder();

                if (bIsMenuOD) {
                    getTxtDateTimeToolbar().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getTitle());
                } else {
                    mOrder.for_date = mMenu.for_date;
                    for (int a = 0; a < mMenu.listTimeModel.size(); a++)
                        if (mMenu.listTimeModel.get(a).isSelected) {
                            updateTimeOrder(mMenu.listTimeModel.get(a));
                            break;
                        }

                    getTxtDateTimeToolbar().setText(BentoNowUtils.getDayTimeSelected(mOrder));
                }

                updateUI();
            }
        });

    }

    private void track() {
        try {
            OrderItem item = mOrder.OrderItems.get(orderIndex);

            JSONObject params = new JSONObject();
            params.put("main", item.items.get(0) == null ? "0" : item.items.get(0).itemId);
            params.put("side1", item.items.get(1) == null ? "0" : item.items.get(1).itemId);
            params.put("side2", item.items.get(2) == null ? "0" : item.items.get(2).itemId);
            params.put("side3", item.items.get(3) == null ? "0" : item.items.get(3).itemId);
            params.put("side4", item.items.get(4) == null ? "0" : item.items.get(4).itemId);

            MixpanelUtils.track("Bento Requested", params);
        } catch (Exception e) {
            DebugUtils.logError(TAG, "track(): " + e.toString());
        }
    }

    private void setDateTime(final boolean bAnimateExit, final boolean bAnimateEnter) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!bShowDateTime) {
                    bShowDateTime = true;
                    getImgDropDownUp().setImageResource(R.drawable.ic_action_navigation_arrow_drop_up);
                    getContainerDateTime().setVisibility(View.VISIBLE);
                    Animation dateInAnimation = AnimationUtils.loadAnimation(BuildBentoActivity.this, R.anim.date_time_in);
                    if (bAnimateEnter)
                        getContainerDateTimeSelection().startAnimation(dateInAnimation);
                } else if (bIsMenuAlreadySelected) {
                    bShowDateTime = false;
                    getImgDropDownUp().setImageResource(R.drawable.ic_action_navigation_arrow_drop_down);
                    Animation dateOutAnimation = AnimationUtils.loadAnimation(BuildBentoActivity.this, R.anim.top_slide_out);
                    dateOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getContainerDateTime().setVisibility(View.GONE);
                                }
                            });
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    if (bAnimateExit)
                        getContainerDateTimeSelection().startAnimation(dateOutAnimation);
                    else
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getContainerDateTime().setVisibility(View.GONE);
                            }
                        });
                }
            }
        });
    }

    private void setOAHashTimer(final long lMilliSeconds) {
        if (lMilliSeconds == 0) {
            if (mCountDown != null) {
                mCountDown.cancel();
                mCountDown = null;
            }
        } else {
            if (mCountDown != null)
                mCountDown.cancel();
            lMilliSecondsRemaining = lMilliSeconds;

            mCountDown = new CountDownTimer(lMilliSeconds, 1000) {
                @Override
                public void onTick(long l) {
                    lMilliSecondsRemaining = lMilliSecondsRemaining - 1000;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getBtnContinue().setText(getString(R.string.build_bento_btn_time_oa, sContinueBtn, IosCopyDao.get("oa-countdown-label"),
                                    AndroidUtil.getMinFromMillis(lMilliSecondsRemaining), AndroidUtil.getSecondsFromMillis(lMilliSecondsRemaining)));
                        }

                    });
                }

                @Override
                public void onFinish() {
                    Intent intent = new Intent(BuildBentoActivity.this, DeliveryLocationActivity.class);
                    intent.putExtra(ConstantUtils.TAG_OPEN_SCREEN, ConstantUtils.optOpenScreen.BUILD_BENTO);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    finish();
                    startActivity(intent);
                }
            };

            mCountDown.start();
        }
    }

    private void updateWidgetSelection() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bIsAsapChecked) {
                    getCheckBoxOD().setBackgroundDrawable(getResources().getDrawable(R.drawable.bento_btn_check_on_holo_light));
                    getCheckBoxOA().setBackgroundDrawable(getResources().getDrawable(R.drawable.bento_btn_check_off_holo_light));
                    getTxtOdTitle().setVisibility(View.VISIBLE);
                    getTxtOdDescription().setVisibility(View.VISIBLE);
                    getContainerSpinnerDayTimeOa().setVisibility(View.GONE);
                } else {
                    getCheckBoxOD().setBackgroundDrawable(getResources().getDrawable(R.drawable.bento_btn_check_off_holo_light));
                    getCheckBoxOA().setBackgroundDrawable(getResources().getDrawable(R.drawable.bento_btn_check_on_holo_light));
                    getTxtOdTitle().setVisibility(View.GONE);
                    getTxtOdDescription().setVisibility(View.GONE);
                    getContainerSpinnerDayTimeOa().setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onMapNoService() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());
        finish();
        BentoNowUtils.openDeliveryLocationScreen(BuildBentoActivity.this, ConstantUtils.optOpenScreen.BUILD_BENTO);
    }

    @Override
    public void onBuild() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());

        if (MenuDao.hasNewMenus(aMenusId)) {
            DebugUtils.logDebug(TAG, "Should change from Missed Menus " + aMenusId.toString());
            mOrderDao.cleanUp();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateWidget();
                    createOrder();
                    updateUI();
                }
            });
        } else if (!SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.POD_MODE).equals(SettingsDao.getCurrent().pod_mode)) {
            DebugUtils.logDebug(TAG, "Should change from Pod Mode " + SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.POD_MODE) + " to " + SettingsDao.getCurrent().pod_mode);
            WidgetsUtils.createLongToast(R.string.error_restarting_app);
            mOrderDao.cleanUp();
            recreate();
        }
    }

    @Override
    public void onClosedWall() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());
        finish();
        BentoNowUtils.openErrorActivity(BuildBentoActivity.this);
    }

    @Override
    public void onSold() {
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.STORE_STATUS, MenuDao.gateKeeper.getAppState());
        finish();
        BentoNowUtils.openErrorActivity(BuildBentoActivity.this);
    }

    @Override
    public void onConnectService() {
        DebugUtils.logDebug(TAG, "Service Connected");
        mBentoService.setServiceListener(BuildBentoActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BentoCustomerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        trackViewedScreen("Viewed Custom Home Screen");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) {
            mBentoService.setServiceListener(null);
            unbindService(mConnection);
            mBound = false;
        }

        if (mCountDown != null) {
            mCountDown.cancel();
            mCountDown = null;
        }
    }


    private AutoFitTxtView getTxtPromoName() {
        if (txtPromoName == null)
            txtPromoName = (AutoFitTxtView) findViewById(R.id.txt_promo_name);
        return txtPromoName;
    }

    private AutoFitTxtView getTxtEta() {
        if (txtEta == null)
            txtEta = (AutoFitTxtView) findViewById(R.id.txt_promo_eta);
        return txtEta;
    }

    private AutoFitTxtView getBtnAddOn() {
        if (btnAddOn == null)
            btnAddOn = (AutoFitTxtView) findViewById(R.id.btn_add_on_add_on);
        return btnAddOn;
    }

    private AutoFitTxtView getTxtOdDescription() {
        if (txtOdDescription == null)
            txtOdDescription = (AutoFitTxtView) findViewById(R.id.txt_date_time_description);
        return txtOdDescription;
    }

    private AutoFitTxtView getTxtOaHeader() {
        if (txtOaHeader == null)
            txtOaHeader = (AutoFitTxtView) findViewById(R.id.txt_oa_header);
        return txtOaHeader;
    }

    private AutoFitTxtView getTxtOdHeader() {
        if (txtOdHeader == null)
            txtOdHeader = (AutoFitTxtView) findViewById(R.id.txt_od_header);
        return txtOdHeader;
    }

    private BackendAutoFitTextView getBtnContinue() {
        if (btnContinue == null)
            btnContinue = (BackendAutoFitTextView) findViewById(R.id.btn_continue);
        return btnContinue;
    }

    private BackendTextView getTxtAddMain() {
        if (txtAddMain == null)
            txtAddMain = (BackendTextView) findViewById(R.id.txt_add_main);
        return txtAddMain;
    }

    private BackendTextView getTxtAddSide1() {
        if (txtAddSide1 == null)
            txtAddSide1 = (BackendTextView) findViewById(R.id.txt_add_side1);
        return txtAddSide1;
    }

    private BackendTextView getTxtAddSide2() {
        if (txtAddSide2 == null)
            txtAddSide2 = (BackendTextView) findViewById(R.id.txt_add_side2);
        return txtAddSide2;
    }

    private BackendTextView getTxtAddSide3() {
        if (txtAddSide3 == null)
            txtAddSide3 = (BackendTextView) findViewById(R.id.txt_add_side3);
        return txtAddSide3;
    }

    private BackendTextView getTxtAddSide4() {
        if (txtAddSide4 == null)
            txtAddSide4 = (BackendTextView) findViewById(R.id.txt_add_side4);
        return txtAddSide4;
    }

    private BackendAutoFitTextView getBtnAddAnotherBento() {
        if (btnAddAnotherBento == null)
            btnAddAnotherBento = (BackendAutoFitTextView) findViewById(R.id.btn_add_another_bento);
        return btnAddAnotherBento;
    }

    private LinearLayout getLayoutAddOns() {
        if (layoutAddOns == null)
            layoutAddOns = (LinearLayout) findViewById(R.id.layout_add_ons);
        return layoutAddOns;
    }

    private ImageView getActionbarLeftBtn() {
        if (actionbarLeftBtn == null)
            actionbarLeftBtn = (ImageView) findViewById(R.id.actionbar_left_btn);
        return actionbarLeftBtn;
    }

    private ImageView getActionbarRightBtn() {
        if (actionbarRightBtn == null)
            actionbarRightBtn = (ImageView) findViewById(R.id.actionbar_right_btn);
        return actionbarRightBtn;
    }

    private ImageView getImgMain() {
        if (imgMain == null)
            imgMain = (ImageView) findViewById(R.id.img_main);
        return imgMain;
    }

    private ImageView getImgSide1() {
        if (imgSide1 == null)
            imgSide1 = (ImageView) findViewById(R.id.img_side1);
        return imgSide1;
    }

    private ImageView getImgSide14() {
        if (imgSide14 == null)
            imgSide14 = (ImageView) findViewById(R.id.img_side1_four);
        return imgSide14;
    }

    private ImageView getImgSide2() {
        if (imgSide2 == null)
            imgSide2 = (ImageView) findViewById(R.id.img_side2);
        return imgSide2;
    }


    private ImageView getImgSide24() {
        if (imgSide24 == null)
            imgSide24 = (ImageView) findViewById(R.id.img_side2_four);
        return imgSide24;
    }

    private ImageView getImgSide3() {
        if (imgSide3 == null)
            imgSide3 = (ImageView) findViewById(R.id.img_side3);
        return imgSide3;
    }

    private ImageView getImgSide34() {
        if (imgSide34 == null)
            imgSide34 = (ImageView) findViewById(R.id.img_side3_four);
        return imgSide34;
    }

    private ImageView getImgSide4() {
        if (imgSide4 == null)
            imgSide4 = (ImageView) findViewById(R.id.img_side4);
        return imgSide4;
    }

    private ImageView getImgMain4() {
        if (imgMain4 == null)
            imgMain4 = (ImageView) findViewById(R.id.img_main_four);
        return imgMain4;
    }

    private ImageView getImgMainSoldOut() {
        if (imgMainSoldOut == null)
            imgMainSoldOut = (ImageView) findViewById(R.id.img_main_sold_out);
        return imgMainSoldOut;
    }

    private ImageView getImgSide1SoldOut() {
        if (imgSide1SoldOut == null)
            imgSide1SoldOut = (ImageView) findViewById(R.id.img_side1_sold_out);
        return imgSide1SoldOut;
    }


    private ImageView getImgSide14SoldOut() {
        if (imgSide14SoldOut == null)
            imgSide14SoldOut = (ImageView) findViewById(R.id.img_side1_sold_out_four);
        return imgSide14SoldOut;
    }

    private ImageView getImgSide2SoldOut() {
        if (imgSide2SoldOut == null)
            imgSide2SoldOut = (ImageView) findViewById(R.id.img_side2_sold_out);
        return imgSide2SoldOut;
    }

    private ImageView getImgSide24SoldOut() {
        if (imgSide24SoldOut == null)
            imgSide24SoldOut = (ImageView) findViewById(R.id.img_side2_sold_out_four);
        return imgSide24SoldOut;
    }

    private ImageView getImgSide3SoldOut() {
        if (imgSide3SoldOut == null)
            imgSide3SoldOut = (ImageView) findViewById(R.id.img_side3_sold_out);
        return imgSide3SoldOut;
    }

    private ImageView getImgSide34SoldOut() {
        if (imgSide34SoldOut == null)
            imgSide34SoldOut = (ImageView) findViewById(R.id.img_side3_sold_out_four);
        return imgSide34SoldOut;
    }

    private ImageView getImgSide4SoldOut() {
        if (imgSide4SoldOut == null)
            imgSide4SoldOut = (ImageView) findViewById(R.id.img_side4_sold_out);
        return imgSide4SoldOut;
    }

    private ImageView getImgMainSoldOut4() {
        if (imgMainSoldOut4 == null)
            imgMainSoldOut4 = (ImageView) findViewById(R.id.img_main_sold_out_four);
        return imgMainSoldOut4;
    }

    private ImageView getImgDropDownUp() {
        if (imgDropDownUp == null)
            imgDropDownUp = (ImageView) findViewById(R.id.img_drop_down_up);
        return imgDropDownUp;
    }

    private ImageView getImgDividerStatus() {
        if (imgDividerStatus == null)
            imgDividerStatus = (ImageView) findViewById(R.id.img_divider_status);
        return imgDividerStatus;
    }

    private TextView getTxtNumBento() {
        if (txtNumBento == null)
            txtNumBento = (TextView) findViewById(R.id.actionbar_right_badge);
        return txtNumBento;
    }

    private TextView getTxtTitleMain() {
        if (txtTitleMain == null)
            txtTitleMain = (TextView) findViewById(R.id.txt_main_title);
        return txtTitleMain;
    }

    private TextView getTxtTitleSide1() {
        if (txtTitleSide1 == null)
            txtTitleSide1 = (TextView) findViewById(R.id.txt_side1_title);
        return txtTitleSide1;
    }

    private TextView getTxtTitleSide14() {
        if (txtTitleSide14 == null)
            txtTitleSide14 = (TextView) findViewById(R.id.txt_side1_title_four);
        return txtTitleSide14;
    }

    private TextView getTxtTitleSide2() {
        if (txtTitleSide2 == null)
            txtTitleSide2 = (TextView) findViewById(R.id.txt_side2_title);
        return txtTitleSide2;
    }

    private TextView getTxtTitleSide24() {
        if (txtTitleSide24 == null)
            txtTitleSide24 = (TextView) findViewById(R.id.txt_side2_title_four);
        return txtTitleSide24;
    }

    private TextView getTxtTitleSide3() {
        if (txtTitleSide3 == null)
            txtTitleSide3 = (TextView) findViewById(R.id.txt_side3_title);
        return txtTitleSide3;
    }

    private TextView getTxtTitleSide34() {
        if (txtTitleSide34 == null)
            txtTitleSide34 = (TextView) findViewById(R.id.txt_side3_title_four);
        return txtTitleSide34;
    }


    private TextView getTxtTitleSide4() {
        if (txtTitleSide4 == null)
            txtTitleSide4 = (TextView) findViewById(R.id.txt_side4_title);
        return txtTitleSide4;
    }

    private TextView getTxtTitleMain4() {
        if (txtTitleMain4 == null)
            txtTitleMain4 = (TextView) findViewById(R.id.txt_main_title_four);
        return txtTitleMain4;
    }

    private TextView getTxtOdTitle() {
        if (txtOdTitle == null)
            txtOdTitle = (TextView) findViewById(R.id.txt_od_title);
        return txtOdTitle;
    }

    private RelativeLayout getContainerMainTitle() {
        if (containerMainTitle == null)
            containerMainTitle = (RelativeLayout) findViewById(R.id.container_main_title);
        return containerMainTitle;
    }

    private RelativeLayout getContainerSide1Title() {
        if (containerSide1Title == null)
            containerSide1Title = (RelativeLayout) findViewById(R.id.container_side1_title);
        return containerSide1Title;
    }

    private RelativeLayout getContainerSide14Title() {
        if (containerSide14Title == null)
            containerSide14Title = (RelativeLayout) findViewById(R.id.container_side1_title_four);
        return containerSide14Title;
    }

    private RelativeLayout getContainerSide2Title() {
        if (containerSide2Title == null)
            containerSide2Title = (RelativeLayout) findViewById(R.id.container_side2_title);
        return containerSide2Title;
    }

    private RelativeLayout getContainerSide24Title() {
        if (containerSide24Title == null)
            containerSide24Title = (RelativeLayout) findViewById(R.id.container_side2_title_four);
        return containerSide24Title;
    }

    private RelativeLayout getContainerSide3Title() {
        if (containerSide3Title == null)
            containerSide3Title = (RelativeLayout) findViewById(R.id.container_side3_title);
        return containerSide3Title;
    }

    private RelativeLayout getContainerSide34Title() {
        if (containerSide34Title == null)
            containerSide34Title = (RelativeLayout) findViewById(R.id.container_side3_title_four);
        return containerSide34Title;
    }

    private RelativeLayout getContainerSide4Title() {
        if (containerSide4Title == null)
            containerSide4Title = (RelativeLayout) findViewById(R.id.container_side4_title);
        return containerSide4Title;
    }

    private RelativeLayout getContainerMainTitle4() {
        if (containerMainTitle4 == null)
            containerMainTitle4 = (RelativeLayout) findViewById(R.id.container_main_title_four);
        return containerMainTitle4;
    }

    private RelativeLayout getLayoutPodFive() {
        if (layoutPodFive == null)
            layoutPodFive = (RelativeLayout) findViewById(R.id.layout_pod_five);
        return layoutPodFive;
    }

    private RelativeLayout getLayoutPodFour() {
        if (layoutPodFour == null)
            layoutPodFour = (RelativeLayout) findViewById(R.id.layout_pod_four);
        return layoutPodFour;
    }

    private FrameLayout getLayoutDateTime() {
        if (layoutDateTime == null)
            layoutDateTime = (FrameLayout) findViewById(R.id.layout_date_time);
        return layoutDateTime;
    }

    private FrameLayout getContainerCancelWidget() {
        if (containerCancelWidget == null)
            containerCancelWidget = (FrameLayout) findViewById(R.id.container_cancel_widget);
        return containerCancelWidget;
    }

    private RelativeLayout getContainerDateTime() {
        if (containerDateTime == null)
            containerDateTime = (RelativeLayout) findViewById(R.id.container_date_time);
        return containerDateTime;
    }

    private LinearLayout getContainerDateTimeSelection() {
        if (containerDateTimeSelection == null)
            containerDateTimeSelection = (LinearLayout) findViewById(R.id.container_date_time_selection);
        return containerDateTimeSelection;
    }

    private LinearLayout getContainerSpinnerDayTimeOa() {
        if (containerSpinnerDayTimeOa == null)
            containerSpinnerDayTimeOa = (LinearLayout) findViewById(R.id.container_spinner_day_time_oa);
        return containerSpinnerDayTimeOa;
    }

    private LinearLayout getWrapperOd() {
        if (wrapperOd == null)
            wrapperOd = (LinearLayout) findViewById(R.id.wrapper_od);
        return wrapperOd;
    }

    private LinearLayout getWrapperOa() {
        if (wrapperOa == null)
            wrapperOa = (LinearLayout) findViewById(R.id.wrapper_oa);
        return wrapperOa;
    }

    private Spinner getSpinnerDate() {
        if (spinnerDate == null)
            spinnerDate = (Spinner) findViewById(R.id.spinner_date);
        return spinnerDate;
    }

    private Spinner getSpinnerTime() {
        if (spinnerTime == null)
            spinnerTime = (Spinner) findViewById(R.id.spinner_time);
        return spinnerTime;
    }

    private ImageView getCheckBoxOD() {
        if (checkBoxOD == null)
            checkBoxOD = (ImageView) findViewById(R.id.checkbox_od);
        return checkBoxOD;
    }

    private ImageView getCheckBoxOA() {
        if (checkBoxOA == null)
            checkBoxOA = (ImageView) findViewById(R.id.checkbox_oa);
        return checkBoxOA;
    }

    private SpinnerOATimeListAdapter getSpinnerTimeAdapter() {
        if (mSpinnerTimeAdapter == null)
            mSpinnerTimeAdapter = new SpinnerOATimeListAdapter(BuildBentoActivity.this);
        return mSpinnerTimeAdapter;
    }

    private SpinnerOADayListAdapter getSpinnerDayAdapter() {
        if (mSpinnerDayAdapter == null)
            mSpinnerDayAdapter = new SpinnerOADayListAdapter(BuildBentoActivity.this);
        return mSpinnerDayAdapter;
    }

    private AutoFitTxtView getTxtDateTimeToolbar() {
        if (txtDateTimeToolbar == null)
            txtDateTimeToolbar = (AutoFitTxtView) findViewById(R.id.txt_date_time_toolbar);
        return txtDateTimeToolbar;
    }

    private ButtonFlat getButtonCancel() {
        if (buttonCancel == null)
            buttonCancel = (ButtonFlat) findViewById(R.id.button_cancel_widget);
        return buttonCancel;
    }

    private ButtonFlat getButtonAccept() {
        if (buttonAccept == null)
            buttonAccept = (ButtonFlat) findViewById(R.id.button_accept_widget);
        return buttonAccept;
    }
}
