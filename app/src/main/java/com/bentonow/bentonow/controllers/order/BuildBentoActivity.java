package com.bentonow.bentonow.controllers.order;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bentonow.bentonow.R;
import com.bentonow.bentonow.Utils.AndroidUtil;
import com.bentonow.bentonow.Utils.BentoNowUtils;
import com.bentonow.bentonow.Utils.BentoRestClient;
import com.bentonow.bentonow.Utils.ConstantUtils;
import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.Utils.GoogleAnalyticsUtil;
import com.bentonow.bentonow.Utils.ImageUtils;
import com.bentonow.bentonow.Utils.MixpanelUtils;
import com.bentonow.bentonow.Utils.SharedPreferencesUtil;
import com.bentonow.bentonow.Utils.WidgetsUtils;
import com.bentonow.bentonow.controllers.BaseFragmentActivity;
import com.bentonow.bentonow.controllers.adapter.NextDayMainListAdapter;
import com.bentonow.bentonow.controllers.adapter.SpinnerOADayListAdapter;
import com.bentonow.bentonow.controllers.adapter.SpinnerOATimeListAdapter;
import com.bentonow.bentonow.controllers.dialog.ConfirmationDialog;
import com.bentonow.bentonow.controllers.geolocation.DeliveryLocationActivity;
import com.bentonow.bentonow.dao.DishDao;
import com.bentonow.bentonow.dao.IosCopyDao;
import com.bentonow.bentonow.dao.MenuDao;
import com.bentonow.bentonow.dao.SettingsDao;
import com.bentonow.bentonow.listener.InterfaceCustomerService;
import com.bentonow.bentonow.model.DishModel;
import com.bentonow.bentonow.model.Menu;
import com.bentonow.bentonow.model.Order;
import com.bentonow.bentonow.model.User;
import com.bentonow.bentonow.model.menu.TimesModel;
import com.bentonow.bentonow.model.order.OrderItem;
import com.bentonow.bentonow.service.BentoCustomerService;
import com.bentonow.bentonow.ui.AutoFitTxtView;
import com.bentonow.bentonow.ui.BackendAutoFitTextView;
import com.bentonow.bentonow.ui.BackendTextView;
import com.bentonow.bentonow.ui.material.ButtonFlat;
import com.bentonow.bentonow.ui.material.SpinnerMaterial;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.mixpanel.android.mpmetrics.Tweak;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;

public class BuildBentoActivity extends BaseFragmentActivity implements View.OnClickListener, InterfaceCustomerService, AdapterView.OnItemClickListener {

    static final String TAG = "BuildBentoActivity";
    //private static Tweak<Boolean> showBanner = MixpanelAPI.booleanTweak("Show Banner", false);
    private static Tweak<Boolean> showAddons = MixpanelAPI.booleanTweak("Show AddOns", false);
    int iMenuSelected = 0;
    boolean bIsMenuAvailable = false;
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
    private LinearLayout wrapperOaHeader;
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
    private RelativeLayout contentMenuPreview;
    private RelativeLayout contentBuildBento;
    private FrameLayout layoutDateTime;
    private FrameLayout containerCancelWidget;
    private LinearLayout containerDateTimeSelection;
    private LinearLayout containerSpinnerDayTimeOa;
    private LinearLayout wrapperBentoMain;
    private LinearLayout wrapperBentoMainFour;
    private LinearLayout wrapperBentoSide1;
    private LinearLayout wrapperBentoSide1Four;
    private LinearLayout wrapperBentoSide2;
    private LinearLayout wrapperBentoSide2Four;
    private LinearLayout wrapperBentoSide3;
    private LinearLayout wrapperBentoSide3Four;
    private LinearLayout wrapperBentoSide4;
    private TextView txtTitleMain;
    private TextView txtTitleMain4;
    private TextView txtTitleSide1;
    private TextView txtTitleSide14;
    private TextView txtTitleSide2;
    private TextView txtTitleSide24;
    private TextView txtTitleSide3;
    private TextView txtTitleSide34;
    private TextView txtTitleSide4;
    private SpinnerMaterial spinnerDate;
    private SpinnerMaterial spinnerTime;
    private ImageView checkBoxOD;
    private ImageView checkBoxOA;
    private ButtonFlat buttonCancel;
    private ButtonFlat buttonAccept;
    private ListView mListMain;
    private GridView gridSide;
    private ListView mListAddOn;
    private NextDayMainListAdapter mainAdapter;
    private NextDayMainListAdapter sideAdapter;
    private NextDayMainListAdapter addOnAdapter;
    private SpinnerOATimeListAdapter mSpinnerTimeAdapter;
    private SpinnerOADayListAdapter mSpinnerDayAdapter;
    private ConfirmationDialog mDialog;
    private Menu mMenu;
    private Menu mOAPreselectedMenu;
    private Order mOrder;
    private User mCurrentUser;
    private ArrayList<String> aMenusId = new ArrayList<>();
    private CountDownTimer mCountDown;
    private String sContinueBtn;
    private int iNumDishesAdded;
    private long lMilliSecondsRemaining;
    private boolean bSawAddOns = false;
    private boolean bHasAddOns = false;
    private boolean bShowDateTime = false;
    private boolean bIsMenuAlreadySelected = false;
    private boolean bIsAsapChecked = false;
    private boolean bShowAppOnDemand = true;
    private boolean bShowAppOnAhead = true;
    private boolean bAutoCompleteFails;
    private ConstantUtils.optMenuSelected optMenu;

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
        getWrapperOd().setOnClickListener(this);
        getWrapperOaHeader().setOnClickListener(this);
        getWrapperBentoMain().setOnClickListener(this);
        getWrapperBentoMainFour().setOnClickListener(this);
        getWrapperBentoSide1().setOnClickListener(this);
        getWrapperBentoSide1Four().setOnClickListener(this);
        getWrapperBentoSide2().setOnClickListener(this);
        getWrapperBentoSide2Four().setOnClickListener(this);
        getWrapperBentoSide3().setOnClickListener(this);
        getWrapperBentoSide3Four().setOnClickListener(this);
        getWrapperBentoSide4().setOnClickListener(this);

        getListMain().setAdapter(getMainAdapter());
        getListAddOn().setAdapter(getAddOnAdapter());
        getGridSide().setAdapter(getSideAdapter());

        getTxtEta().setText(String.format(getString(R.string.build_bento_eta), MenuDao.eta_min + "-" + MenuDao.eta_max));

        getSpinnerDate().setAdapter(getSpinnerDayAdapter());

        getSpinnerTime().setAdapter(getSpinnerTimeAdapter());

        getSpinnerDate().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (bShowAppOnAhead) {
                    updateDayOASpinner(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.get(i), 0);
                    getSpinnerDayAdapter().iSelectedPosition = i;
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        getSpinnerTime().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int iPosition, long l) {
                if (bShowAppOnAhead) {
                    for (int a = 0; a < mOAPreselectedMenu.listTimeModel.size(); a++) {
                        if (a == iPosition) {
                            mOAPreselectedMenu.listTimeModel.get(a).isSelected = true;
                        } else
                            mOAPreselectedMenu.listTimeModel.get(a).isSelected = false;
                    }
                    getSpinnerTimeAdapter().iSelectedPosition = iPosition;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

        });

        sContinueBtn = IosCopyDao.get("build-title");

        updateWidget();

        mCurrentUser = userDao.getCurrentUser();

        if (mCurrentUser != null)
            getUserInfo();

    }

    @Override
    protected void onResume() {
        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.CLEAR_ORDERS_FROM_SUMMARY)) {
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.CLEAR_ORDERS_FROM_SUMMARY, false);

            forceChangeMenu();
        } else {
            if (mOrder != null) {
                createOrder();
            }
            updateUI();

            if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ON_CONTINUE_FROM_ADD_ON)) {
                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ON_CONTINUE_FROM_ADD_ON, false);
                onContinueOrderPressed();
            }
        }

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);

        GoogleAnalyticsUtil.sendScreenView("Build Your Bento");

        super.onResume();
    }

    public void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bIsMenuAlreadySelected) {
                    if (bShowAppOnAhead) {
                        getTxtOdHeader().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getTitle());
                        getTxtOdDescription().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getText());
                    }

                    getTxtOdHeader().setTextColor(getResources().getColor(optMenu == ConstantUtils.optMenuSelected.ORDER_AHEAD ? R.color.black : R.color.primary));
                    getTxtOaHeader().setTextColor(getResources().getColor(optMenu == ConstantUtils.optMenuSelected.ORDER_AHEAD ? R.color.primary : R.color.black));

                    switch (optMenu) {
                        case ON_DEMAND:
                        case ORDER_AHEAD:
                            getContentMenuPreview().setVisibility(View.GONE);
                            getContentBuildBento().setVisibility(View.VISIBLE);
                            updateDishUI();
                            getTxtEta().setVisibility(optMenu == ConstantUtils.optMenuSelected.ON_DEMAND ? View.VISIBLE : View.GONE);
                            getImgDividerStatus().setVisibility(optMenu == ConstantUtils.optMenuSelected.ON_DEMAND ? View.VISIBLE : View.GONE);
                            getTxtPromoName().setText(String.format(getString(R.string.build_bento_price), BentoNowUtils.getDefaultPriceBento(DishDao.getLowestMainPrice(mMenu))));

                            if (optMenu == ConstantUtils.optMenuSelected.ORDER_AHEAD && bIsMenuAlreadySelected && mMenu.menu_id.equals(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.get(0).menu_id))
                                setOAHashTimer(BentoNowUtils.showOATimer(mMenu));
                            else if (mCountDown != null) {
                                mCountDown.cancel();
                                mCountDown = null;
                            }

                            if (mOrderDao.countCompletedOrders(mOrder) == 0) {
                                getTxtNumBento().setVisibility(View.GONE);
                                getTxtNumBento().setText("0");
                                getActionbarRightBtn().setImageResource(R.drawable.ic_ab_bento);
                                getLayoutAddOns().setVisibility(View.GONE);
                                getBtnContinue().setBackground(getResources().getDrawable(R.drawable.btn_rounded_gray));
                                getBtnAddAnotherBento().setTextColor(getResources().getColor(R.color.btn_green_trans));
                                getBtnAddAnotherBento().setOnClickListener(null);
                                getBtnAddOn().setTextColor(getResources().getColor(R.color.btn_green_trans));
                                getBtnAddOn().setOnClickListener(null);
                                getBtnAddAnotherBento().setText(IosCopyDao.get("build-add-button"));

                                if (mOrderDao.countDishesAdded(mOrder.OrderItems.get(orderIndex)) == 0)
                                    sContinueBtn = IosCopyDao.get("build-title");
                                else
                                    sContinueBtn = IosCopyDao.get("build-button-1");

                            } else {
                                getTxtNumBento().setVisibility(View.VISIBLE);
                                getTxtNumBento().setText(mOrderDao.countCompletedOrders(mOrder) + "");
                                getActionbarRightBtn().setImageResource(R.drawable.ic_ab_bento_completed);
                                getLayoutAddOns().setVisibility(View.VISIBLE);
                                getBtnContinue().setBackground(getResources().getDrawable(R.drawable.btn_rounded_green));
                                getBtnAddOn().setTextColor(getResources().getColor(R.color.btn_green));
                                getBtnAddOn().setOnClickListener(BuildBentoActivity.this);
                                getBtnAddAnotherBento().setTextColor(getResources().getColor(R.color.btn_green));
                                getBtnAddAnotherBento().setOnClickListener(BuildBentoActivity.this);
                                sContinueBtn = IosCopyDao.get("build-button-2");

                                if (mBentoDao.isBentoComplete(mOrder.OrderItems.get(orderIndex)) || orderIndex == 0)
                                    getBtnAddAnotherBento().setText(IosCopyDao.get("build-add-button"));
                                else if (mOrderDao.countDishesAdded(mOrder.OrderItems.get(orderIndex)) == 0)
                                    getBtnAddAnotherBento().setText(IosCopyDao.get("build-title"));
                                else
                                    getBtnAddAnotherBento().setText(IosCopyDao.get("build-button-1"));
                            }

                            getBtnContinue().setText(sContinueBtn);

                            getBtnAddOn().setVisibility(bHasAddOns ? View.VISIBLE : View.GONE);
                            break;
                        case MENU_PREVIEW:
                            getButtonCancel().setVisibility(bIsMenuAlreadySelected ? View.VISIBLE : View.GONE);
                            getTxtNumBento().setVisibility(View.GONE);
                            getActionbarRightBtn().setImageResource(R.drawable.ic_ab_bento);
                            getTxtDateTimeToolbar().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getTitle());

                            if (getMainAdapter().isEmpty()) {
                                for (DishModel dishModel : mMenu.dishModels) {
                                    switch (dishModel.type) {
                                        case "main":
                                            getMainAdapter().add(dishModel);
                                            break;
                                        case "side":
                                            getSideAdapter().add(dishModel);
                                            break;
                                        case "addon":
                                            getAddOnAdapter().add(dishModel);
                                            break;
                                        default:
                                            DebugUtils.logError(TAG, "Unknown Type: " + dishModel.type + " Dish: " + dishModel.name);
                                            break;
                                    }

                                }

                                getMainAdapter().notifyDataSetChanged();
                                getSideAdapter().notifyDataSetChanged();
                                getAddOnAdapter().notifyDataSetChanged();

                                getListMain().setOnItemClickListener(BuildBentoActivity.this);
                                getListAddOn().setOnItemClickListener(BuildBentoActivity.this);
                                getGridSide().setOnItemClickListener(BuildBentoActivity.this);
                            }

                            getContentMenuPreview().setVisibility(View.VISIBLE);
                            getContentBuildBento().setVisibility(View.GONE);
                            break;
                    }
                }
            }
        });

    }

    private void updateWidget() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSpinnerDayAdapter().clear();
                getSpinnerTimeAdapter().clear();
                bShowAppOnDemand = MenuDao.gateKeeper.getAppOnDemandWidget() != null && (MenuDao.getTodayMenu() != null || MenuDao.gateKeeper.getAppOnDemandWidget().getMenu() != null);
                bShowAppOnAhead = MenuDao.gateKeeper.getAvailableServices() != null && MenuDao.gateKeeper.getAvailableServices().mOrderAhead != null && !MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.isEmpty();

                if (bShowAppOnDemand) {
                    getWrapperOd().setVisibility(View.VISIBLE);
                    getTxtOdHeader().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getTitle());
                    getTxtOdDescription().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getText());
                } else
                    getWrapperOd().setVisibility(View.GONE);

                if (bShowAppOnAhead) {
                    getWrapperOa().setVisibility(View.VISIBLE);
                    getSpinnerDayAdapter().addAll(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus);
                    getSpinnerDayAdapter().notifyDataSetChanged();
                    getSpinnerTimeAdapter().addAll(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.get(0).listTimeModel);
                    getSpinnerTimeAdapter().notifyDataSetChanged();
                    getTxtOaHeader().setText(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.title);

                } else
                    getWrapperOa().setVisibility(View.GONE);

                if (bShowAppOnDemand && MenuDao.gateKeeper.getAppOnDemandWidget().isSelected()) {
                    optMenu = ConstantUtils.optMenuSelected.ON_DEMAND;
                    bIsAsapChecked = true;
                } else {
                    optMenu = ConstantUtils.optMenuSelected.ORDER_AHEAD;
                    bIsAsapChecked = false;
                }
                updateWidgetSelection();

                setDateTime(true, false);
            }
        });

    }

    private void updateOrderByMenu() {
        mOrderDao.cleanUp();
        getMainAdapter().clear();
        getSideAdapter().clear();
        getAddOnAdapter().clear();
        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.IS_ORDER_AHEAD_MENU, optMenu == ConstantUtils.optMenuSelected.ORDER_AHEAD);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (optMenu) {
                    case ON_DEMAND:
                        mMenu = MenuDao.cloneMenu(MenuDao.getTodayMenu());
                        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ON_DEMAND_AVAILABLE, true);
                        getTxtDateTimeToolbar().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getTitle());
                        //  restartDishUI();
                        if (mMenu != null)
                            createOrder();
                        break;
                    case MENU_PREVIEW:
                        mMenu = MenuDao.cloneMenu(MenuDao.gateKeeper.getAppOnDemandWidget().getMenu());
                        getTxtDateTimeToolbar().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getTitle());
                        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ON_DEMAND_AVAILABLE, false);
                        break;
                    case ORDER_AHEAD:
                        mMenu = MenuDao.cloneMenu(mOAPreselectedMenu);
                        if (mMenu != null) {
                            createOrder();
                            mOrder.for_date = mMenu.for_date;
                            updateTimeOrder((TimesModel) getSpinnerTime().getSelectedItem());
                            getTxtDateTimeToolbar().setText(BentoNowUtils.getDayTimeSelected(mOrder));
                        }
                        //   restartDishUI();
                        break;
                }

                if (mMenu != null) {
                    MenuDao.setCurrentMenu(mMenu);
                    updateUI();
                } else {
                    finish();
                    BentoNowUtils.openDeliveryLocationScreen(BuildBentoActivity.this, ConstantUtils.optOpenScreen.BUILD_BENTO);
                }

            }
        });

    }

    private void createOrder() {
        mOrder = mOrderDao.getCurrentOrder();

        if (mOrder == null) {

            mOrder = mOrderDao.getNewOrder(optMenu == ConstantUtils.optMenuSelected.ON_DEMAND);
            mOrder.MealName = mMenu.meal_name;
            mOrder.MenuType = mMenu.menu_type;
            mOrder.MenuId = mMenu.menu_id;

            mOrderDao.updateOrder(mOrder);
        }

        orderIndex = mOrder.currentOrderItem;

        if (mOrder.OrderItems.size() == 0) {
            mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.CUSTOM_BENTO_BOX));
            mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.ADD_ON));
        }
        bHasAddOns = false;

        for (int a = 0; a < mMenu.dishModels.size(); a++) {
            if (mMenu.dishModels.get(a).type.equals("addon")) {
                bHasAddOns = true;
                break;
            }
        }
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
                                  bIsAsapChecked = optMenu == ConstantUtils.optMenuSelected.ORDER_AHEAD ? false : true;
                                  updateWidgetSelection();

                                  if (optMenu == ConstantUtils.optMenuSelected.ORDER_AHEAD) {
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
                getImgMain().setTag(null);
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
                getImgMainSoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(0), false, optMenu == ConstantUtils.optMenuSelected.ON_DEMAND) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(1) == null || item.items.get(1).name.isEmpty()) {
                getImgSide1().setImageBitmap(null);
                getImgSide1().setTag(null);
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
                getImgSide1SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(1), false, optMenu == ConstantUtils.optMenuSelected.ON_DEMAND) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(2) == null || item.items.get(2).name.isEmpty()) {
                getImgSide2().setImageBitmap(null);
                getImgSide2().setTag(null);
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
                getImgSide2SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(2), false, optMenu == ConstantUtils.optMenuSelected.ON_DEMAND) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(3) == null || item.items.get(3).name.isEmpty()) {
                getImgSide3().setImageBitmap(null);
                getImgSide3().setTag(null);
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
                getImgSide3SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(3), false, optMenu == ConstantUtils.optMenuSelected.ON_DEMAND) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(4) == null || item.items.get(4).name.isEmpty()) {
                getImgSide4().setImageBitmap(null);
                getImgSide4().setTag(null);
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
                getImgSide4SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(4), false, optMenu == ConstantUtils.optMenuSelected.ON_DEMAND) ? View.VISIBLE : View.GONE);
            }
        } else {
            getLayoutPodFive().setVisibility(View.GONE);
            getLayoutPodFour().setVisibility(View.VISIBLE);

            if (item.items.get(0) == null || item.items.get(0).name.isEmpty()) {
                getImgMain4().setImageBitmap(null);
                getImgMain4().setTag(null);
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
                getImgMainSoldOut4().setVisibility(mDishDao.isSoldOut(item.items.get(0), false, optMenu == ConstantUtils.optMenuSelected.ON_DEMAND) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(1) == null || item.items.get(1).name.isEmpty()) {
                getImgSide14().setImageBitmap(null);
                getImgSide14().setTag(null);
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
                getImgSide14SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(1), false, optMenu == ConstantUtils.optMenuSelected.ON_DEMAND) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(2) == null || item.items.get(2).name.isEmpty()) {
                getImgSide24().setImageBitmap(null);
                getImgSide24().setTag(null);
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
                getImgSide24SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(2), false, optMenu == ConstantUtils.optMenuSelected.ON_DEMAND) ? View.VISIBLE : View.GONE);
            }

            if (item.items.get(3) == null || item.items.get(3).name.isEmpty()) {
                getImgSide34().setImageBitmap(null);
                getImgSide34().setTag(null);
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
                getImgSide34SoldOut().setVisibility(mDishDao.isSoldOut(item.items.get(3), false, optMenu == ConstantUtils.optMenuSelected.ON_DEMAND) ? View.VISIBLE : View.GONE);
            }
        }

    }

    private void autocomplete() {
        bAutoCompleteFails = false;
        if (mOrder.OrderItems.get(orderIndex).items.get(0).name.isEmpty()) {
            mOrder.OrderItems.get(orderIndex).items.set(0, mDishDao.updateDishItem(mOrder.OrderItems.get(orderIndex).items.get(0), mDishDao.getFirstAvailable(mMenu, "main", null, optMenu == ConstantUtils.optMenuSelected.ON_DEMAND)));
        }
        int iNumDish = mOrder.OrderItems.get(orderIndex).items.size();

        int[] ids = new int[iNumDish - 1];

        for (int i = 1; i < iNumDish; ++i) {
            if (mOrder.OrderItems.get(orderIndex).items.get(i).name.isEmpty()) {
                DishModel dishModel = mDishDao.getFirstAvailable(mMenu, "side", ids, optMenu == ConstantUtils.optMenuSelected.ON_DEMAND);

                if (dishModel == null) {
                    bAutoCompleteFails = true;
                    updateUI();
                    return;
                }

                ids[i - 1] = dishModel.itemId;

                dishModel = DishDao.clone(dishModel);

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

    private void forceChangeMenu() {
        DebugUtils.logDebug(TAG, "The menu has to be Changed");
        if (bIsAsapChecked) {
            optMenu = MenuDao.gateKeeper.getAppOnDemandWidget().isSelected() ? ConstantUtils.optMenuSelected.ON_DEMAND : ConstantUtils.optMenuSelected.MENU_PREVIEW;
        } else
            optMenu = ConstantUtils.optMenuSelected.ORDER_AHEAD;

        updateOrderByMenu();
    }

    private void setDateTime(final boolean bAnimateExit, final boolean bAnimateEnter) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!bShowDateTime) {
                    bShowDateTime = true;
                    getButtonCancel().setVisibility(bIsMenuAlreadySelected ? View.VISIBLE : View.GONE);
                    restartWidget();
                    getImgDropDownUp().setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_action_navigation_arrow_drop_up));
                    getTxtDateTimeToolbar().setTextColor(getResources().getColor(R.color.dark_blue));
                    getContainerDateTime().setVisibility(View.VISIBLE);
                    Animation dateInAnimation = AnimationUtils.loadAnimation(BuildBentoActivity.this, R.anim.date_time_in);
                    dateInAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, false);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    if (bAnimateEnter)
                        getContainerDateTimeSelection().startAnimation(dateInAnimation);
                    else
                        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);

                } else if (bIsMenuAlreadySelected) {
                    bShowDateTime = false;
                    getImgDropDownUp().setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_action_navigation_arrow_drop_down));
                    getTxtDateTimeToolbar().setTextColor(getResources().getColor(R.color.green_promo));
                    Animation dateOutAnimation = AnimationUtils.loadAnimation(BuildBentoActivity.this, R.anim.top_slide_out);
                    dateOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, false);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
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
                                SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
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
                    getCheckBoxOD().setBackgroundDrawable(getResources().getDrawable(R.drawable.bento_btn_check_on_holo_light_alone));
                    getCheckBoxOA().setBackgroundDrawable(null);
                    getTxtOdHeader().setTextColor(getResources().getColor(R.color.green_promo));
                    getTxtOaHeader().setTextColor(getResources().getColor(R.color.dark_blue));
                    getContainerSpinnerDayTimeOa().setVisibility(View.GONE);
                } else {
                    getCheckBoxOD().setBackgroundDrawable(null);
                    getCheckBoxOA().setBackgroundDrawable(getResources().getDrawable(R.drawable.bento_btn_check_on_holo_light_alone));
                    getTxtOdHeader().setTextColor(getResources().getColor(R.color.dark_blue));
                    getTxtOaHeader().setTextColor(getResources().getColor(R.color.green_promo));
                    getContainerSpinnerDayTimeOa().setVisibility(View.VISIBLE);
                }
            }
        });
    }


    public void onAddMainPressed() {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectMainCustomActivity.class);
            intent.putExtra(Menu.TAG, mMenu);
            startActivity(intent);
        } else
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
    }

    public void onAddSide1Pressed() {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectSideCustomActivity.class);
            intent.putExtra(Menu.TAG, mMenu);
            intent.putExtra("itemIndex", 1);
            startActivity(intent);
        } else
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
    }

    public void onAddSide2Pressed() {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectSideCustomActivity.class);
            intent.putExtra(Menu.TAG, mMenu);
            intent.putExtra("itemIndex", 2);
            startActivity(intent);
        } else
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
    }

    public void onAddSide3Pressed() {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectSideCustomActivity.class);
            intent.putExtra(Menu.TAG, mMenu);
            intent.putExtra("itemIndex", 3);
            startActivity(intent);
        } else
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
    }

    public void onAddSide4Pressed() {
        if (!bShowDateTime) {
            Intent intent = new Intent(this, SelectSideCustomActivity.class);
            intent.putExtra(Menu.TAG, mMenu);
            intent.putExtra("itemIndex", 4);
            startActivity(intent);
        } else
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
    }

    public void onAddAnotherBentoPressed() {
        // restartDishUI();
        if (mDishDao.canCreateAnotherBento(mMenu, mOrder.OrderItems.get(orderIndex).items.size(), optMenu == ConstantUtils.optMenuSelected.ON_DEMAND)) {
            mOrder.OrderItems.add(mBentoDao.getNewBento(ConstantUtils.optItemType.CUSTOM_BENTO_BOX));
            mOrder.currentOrderItem = orderIndex = mOrder.OrderItems.size() - 1;

            mOrderDao.updateOrder(mOrder);
        }

        updateUI();

        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
    }

    public void onContinueOrderPressed() {
        mOrderDao.updateOrder(mOrder);
        MenuDao.setCurrentMenu(mMenu);

        if (orderIndex == 0) {
            if (!mBentoDao.isBentoComplete(mOrder.OrderItems.get(orderIndex))) {
                autoCompleteOrder();
            } else
                openSummaryScreen();
        } else if (mOrderDao.countDishesAdded(mOrder.OrderItems.get(orderIndex)) == 0 || bAutoCompleteFails) {
            bAutoCompleteFails = false;
            mBentoDao.removeBento(mOrder.OrderItems.get(orderIndex).order_pk);
            createOrder();
            openSummaryScreen();
        } else if (mBentoDao.isBentoComplete(mOrder.OrderItems.get(orderIndex))) {
            track();
            openSummaryScreen();
        } else {
            mDialog = new ConfirmationDialog(BuildBentoActivity.this, null, IosCopyDao.get("build-not-complete-text"));
            mDialog.addAcceptButton(IosCopyDao.get("build-not-complete-confirmation-2"), BuildBentoActivity.this);
            mDialog.addCancelButton(IosCopyDao.get("build-not-complete-confirmation-1"), BuildBentoActivity.this);

            mDialog.show();
        }


    }

    private void autoCompleteOrder() {
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
    }

    private void openSummaryScreen() {
        String sSoldOutItems = mOrderDao.calculateSoldOutItems(mOrder, orderIndex, optMenu == ConstantUtils.optMenuSelected.ON_DEMAND);

        if (!sSoldOutItems.isEmpty()) {
            updateUI();
            WidgetsUtils.createShortToast(String.format(getString(R.string.error_sold_out_items), sSoldOutItems));
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
        } else if (bHasAddOns && !bSawAddOns) {
            openAddOnsActivity();
        } else if (BentoNowUtils.isValidCompleteOrder(BuildBentoActivity.this)) {
            track();
            BentoNowUtils.openCompleteOrderActivity(BuildBentoActivity.this, mMenu);
        } else
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
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

    private void restartDishUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

    private void restartSpinnerUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*getSpinnerTime().onDetachedFromWindow();
                getSpinnerTime().setEnabled(true);
                getSpinnerTime().setClickable(true);
                getSpinnerDate().onDetachedFromWindow();
                getSpinnerDate().setEnabled(true);
                getSpinnerDate().setClickable(true);*/
            }
        });
    }

    private void getUserInfo() {
        RequestParams params = new RequestParams();
        params.put("api_token", mCurrentUser.api_token);
        BentoRestClient.get("/user/info", params, new TextHttpResponseHandler() {
            @SuppressWarnings("deprecation")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                DebugUtils.logError(TAG, "getUserInfo:  " + responseString);

                DebugUtils.logError(TAG, "getUserInfo failed: " + responseString + " StatusCode: " + statusCode);
                String sError;

                try {
                    sError = new JSONObject(responseString).getString("error");
                } catch (Exception e) {
                    sError = getString(R.string.error_no_internet_connection);
                    DebugUtils.logError(TAG, "requestPromoCode(): " + e.getLocalizedMessage());
                }

                switch (statusCode) {
                    case 0:// No internet Connection
                        break;
                    case 401:// Invalid Api Token
                        WidgetsUtils.createShortToast("You session is expired, please LogIn again");
                        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ORDER_AHEAD_SUBSCRIPTION, false);
                        break;
                    default:
                        Crashlytics.log(Log.ERROR, "SendOrderError", "Code " + statusCode + " : Response " + responseString + " : Parsing " + sError);
                        break;
                }

            }

            @SuppressWarnings("deprecation")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                DebugUtils.logDebug(TAG, "getUserInfo: " + responseString);
                try {
                    User mUserInfo = new Gson().fromJson(responseString, User.class);
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ORDER_AHEAD_SUBSCRIPTION, mUserInfo.has_oa_subscription.equals("1"));
                } catch (Exception ex) {
                    DebugUtils.logError(TAG, "getUserInfo(): " + ex.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK)) {
            SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, false);
            switch (v.getId()) {
                case R.id.actionbar_left_btn:
                    BentoNowUtils.openSettingsActivity(BuildBentoActivity.this);
                    break;
                case R.id.actionbar_right_btn:
                    if (!bShowDateTime && optMenu != ConstantUtils.optMenuSelected.MENU_PREVIEW)
                        if (mBentoDao.isBentoComplete(mOrder.OrderItems.get(orderIndex))) {
                            onContinueOrderPressed();
                        } else {
                            mDialog = new ConfirmationDialog(BuildBentoActivity.this, null, IosCopyDao.get("build-not-complete-text"));
                            mDialog.addAcceptButton(IosCopyDao.get("build-not-complete-confirmation-2"), BuildBentoActivity.this);
                            mDialog.addCancelButton(IosCopyDao.get("build-not-complete-confirmation-1"), BuildBentoActivity.this);

                            mDialog.show();
                        }
                    else
                        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                    break;
                case R.id.button_accept:
                    if (!bShowDateTime)
                        autocomplete();

                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                    break;
                case R.id.btn_continue:
                    if (!bShowDateTime)
                        onContinueOrderPressed();

                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                    break;
                case R.id.btn_add_on_add_on:
                    if (!bShowDateTime)
                        openAddOnsActivity();
                    else
                        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                    break;
                case R.id.btn_add_another_bento:
                    if (!bShowDateTime) {
                        if (mBentoDao.isBentoComplete(mOrder.OrderItems.get(orderIndex)))
                            onAddAnotherBentoPressed();
                        else
                            autoCompleteOrder();
                    }
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                    break;
                case R.id.layout_date_time:
                    setDateTime(true, true);
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                    break;
                case R.id.button_accept_widget:
                    if (bShowAppOnAhead || bShowAppOnDemand) {
                        boolean bChangeMenu = false;
                        if (!bIsMenuAlreadySelected) {
                            bIsMenuAlreadySelected = true;
                            forceChangeMenu();
                            setDateTime(true, true);
                        } else {
                            switch (optMenu) {
                                case MENU_PREVIEW:
                                case ON_DEMAND:
                                    if (!bIsAsapChecked)
                                        bChangeMenu = true;
                                    break;
                                case ORDER_AHEAD:
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
                                    break;
                            }

                            if (bChangeMenu) {
                                if (mDishDao.getNumDishes() == 0) {
                                    forceChangeMenu();
                                } else {
                                    mDialog = new ConfirmationDialog(BuildBentoActivity.this, IosCopyDao.get("oa-change-warning-title"), IosCopyDao.get("oa-change-warning-text"));
                                    mDialog.addAcceptButton(IosCopyDao.get("build-not-complete-confirmation-2"), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            forceChangeMenu();
                                        }
                                    });
                                    mDialog.addCancelButton(IosCopyDao.get("build-not-complete-confirmation-1"), new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                }
                                            }

                                    );
                                    mDialog.show();
                                }
                            }

                            setDateTime(!bChangeMenu, true);
                        }
                    } else
                        mBentoService.getBentoData(true);

                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                    break;
                case R.id.button_cancel_widget:
                    setDateTime(true, true);
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                    break;
                case R.id.container_cancel_widget:
                    setDateTime(true, true);
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                    break;
                case R.id.wrapper_od:
                    if (!bIsAsapChecked) {
                        bIsAsapChecked = true;
                        updateWidgetSelection();
                    }
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                    break;
                case R.id.wrapper_oa_header:
                    if (bIsAsapChecked) {
                        bIsAsapChecked = false;
                        updateWidgetSelection();
                    }
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                    break;
                case R.id.build_main:
                case R.id.build_main_four:
                    onAddMainPressed();
                    break;
                case R.id.btn_bento_side_1:
                case R.id.btn_bento_side_1_four:
                    onAddSide1Pressed();
                    break;
                case R.id.btn_bento_side_2:
                case R.id.btn_bento_side_2_four:
                    onAddSide2Pressed();
                    break;
                case R.id.btn_bento_side_3:
                case R.id.btn_bento_side_3_four:
                    onAddSide3Pressed();
                    break;
                case R.id.btn_bento_side_4:
                    onAddSide4Pressed();
                    break;
                default:
                    DebugUtils.logError(TAG, String.valueOf(v.getId()));
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ENABLE_BUILD_BENTO_CLICK, true);
                    break;
            }
        }
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

        if (!bShowAppOnAhead && !bShowAppOnDemand) {
            updateWidget();
        } else {
            if (bIsMenuAlreadySelected) {
                if (bShowAppOnAhead) {
                    if (aMenusId == null || aMenusId.isEmpty()) {
                        aMenusId = MenuDao.getCurrentMenuIds();
                    } else if (MenuDao.hasNewMenus(aMenusId)) {
                        DebugUtils.logError(TAG, "Should change from Missed Menus " + aMenusId.toString());

                        iMenuSelected = 0;
                        bIsMenuAvailable = false;
                        // TODO Verify this logic
                        switch (optMenu) {
                            case ORDER_AHEAD:
                                for (int a = 0; a < MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.size(); a++) {
                                    if (mMenu.menu_id.equals(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.get(a).menu_id)) {
                                        iMenuSelected = a;
                                        bIsMenuAvailable = true;
                                    }
                                }

                                if (!bIsMenuAvailable) {
                                    updateWidget();
                                    forceChangeMenu();
                                }
                            default:
                                if (bIsMenuAvailable)
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getSpinnerDayAdapter().clear();
                                            getSpinnerDayAdapter().addAll(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus);
                                            getSpinnerDayAdapter().notifyDataSetChanged();
                                            getSpinnerDate().setSelection(iMenuSelected);
                                            getSpinnerTimeAdapter().addAll(MenuDao.gateKeeper.getAvailableServices().mOrderAhead.availableMenus.get(iMenuSelected).listTimeModel);
                                            getSpinnerTimeAdapter().notifyDataSetChanged();
                                        }
                                    });
                                break;
                        }
                    }
                }
                if (bShowAppOnDemand) {
                    if (SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ON_DEMAND_AVAILABLE) != MenuDao.gateKeeper.getAppOnDemandWidget().isSelected()) {
                        DebugUtils.logDebug(TAG, "Should change from Demand Change from: " + SharedPreferencesUtil.getBooleanPreference(SharedPreferencesUtil.ON_DEMAND_AVAILABLE) + " to " + MenuDao.gateKeeper.getAppOnDemandWidget().isSelected());
                        switch (optMenu) {
                            case ON_DEMAND:
                            case MENU_PREVIEW:
                                optMenu = MenuDao.gateKeeper.getAppOnDemandWidget().isSelected() ? ConstantUtils.optMenuSelected.ON_DEMAND : ConstantUtils.optMenuSelected.MENU_PREVIEW;
                                updateOrderByMenu();
                                setDateTime(true, true);
                                break;
                            default:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.ON_DEMAND_AVAILABLE, MenuDao.gateKeeper.getAppOnDemandWidget().isSelected());
                                        getTxtOdHeader().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getTitle());
                                        getTxtOdDescription().setText(MenuDao.gateKeeper.getAppOnDemandWidget().getText());
                                    }
                                });
                                break;
                        }
                    }
                }
                if (SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.POD_MODE).isEmpty()) {
                    SharedPreferencesUtil.setAppPreference(SharedPreferencesUtil.POD_MODE, SettingsDao.getCurrent().pod_mode);
                } else if (!SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.POD_MODE).equals(SettingsDao.getCurrent().pod_mode)) {
                    DebugUtils.logError(TAG, "Should change from Pod Mode " + SharedPreferencesUtil.getStringPreference(SharedPreferencesUtil.POD_MODE) + " to " + SettingsDao.getCurrent().pod_mode);
                    recreate();
                }
            }
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            DishModel mDish = (DishModel) parent.getAdapter().getItem(position);
            DebugUtils.logDebug(TAG, "Type: " + mDish.type + " Name: " + mDish.name);

            switch (mDish.type) {
                case "main":
                    getMainAdapter().setCurrentSelected(mDish);
                    getSideAdapter().setCurrentSelected(null);
                    getAddOnAdapter().setCurrentSelected(null);
                    break;
                case "side":
                    getMainAdapter().setCurrentSelected(null);
                    getSideAdapter().setCurrentSelected(mDish);
                    getAddOnAdapter().setCurrentSelected(null);
                    break;
                case "addon":
                    getMainAdapter().setCurrentSelected(null);
                    getSideAdapter().setCurrentSelected(null);
                    getAddOnAdapter().setCurrentSelected(mDish);
                    break;
                default:
                    DebugUtils.logError(TAG, "Unknown Type: " + mDish.type + " Dish: " + mDish.name);
                    break;
            }

            getSideAdapter().notifyDataSetChanged();
            getAddOnAdapter().notifyDataSetChanged();
            getMainAdapter().notifyDataSetChanged();

        } catch (Exception ex) {
            DebugUtils.logError(TAG, "OnClick");
        }

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

    private RelativeLayout getContentMenuPreview() {
        if (contentMenuPreview == null)
            contentMenuPreview = (RelativeLayout) findViewById(R.id.content_menu_preview);
        return contentMenuPreview;
    }

    private RelativeLayout getContentBuildBento() {
        if (contentBuildBento == null)
            contentBuildBento = (RelativeLayout) findViewById(R.id.content_build_bento);
        return contentBuildBento;
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

    private LinearLayout getWrapperBentoMain() {
        if (wrapperBentoMain == null)
            wrapperBentoMain = (LinearLayout) findViewById(R.id.build_main);
        return wrapperBentoMain;
    }

    private LinearLayout getWrapperBentoMainFour() {
        if (wrapperBentoMainFour == null)
            wrapperBentoMainFour = (LinearLayout) findViewById(R.id.build_main_four);
        return wrapperBentoMainFour;
    }

    private LinearLayout getWrapperBentoSide1() {
        if (wrapperBentoSide1 == null)
            wrapperBentoSide1 = (LinearLayout) findViewById(R.id.btn_bento_side_1);
        return wrapperBentoSide1;
    }

    private LinearLayout getWrapperBentoSide1Four() {
        if (wrapperBentoSide1Four == null)
            wrapperBentoSide1Four = (LinearLayout) findViewById(R.id.btn_bento_side_1_four);
        return wrapperBentoSide1Four;
    }

    private LinearLayout getWrapperBentoSide2() {
        if (wrapperBentoSide2 == null)
            wrapperBentoSide2 = (LinearLayout) findViewById(R.id.btn_bento_side_2);
        return wrapperBentoSide2;
    }

    private LinearLayout getWrapperBentoSide2Four() {
        if (wrapperBentoSide2Four == null)
            wrapperBentoSide2Four = (LinearLayout) findViewById(R.id.btn_bento_side_2_four);
        return wrapperBentoSide2Four;
    }

    private LinearLayout getWrapperBentoSide3() {
        if (wrapperBentoSide3 == null)
            wrapperBentoSide3 = (LinearLayout) findViewById(R.id.btn_bento_side_3);
        return wrapperBentoSide3;
    }

    private LinearLayout getWrapperBentoSide3Four() {
        if (wrapperBentoSide3Four == null)
            wrapperBentoSide3Four = (LinearLayout) findViewById(R.id.btn_bento_side_3_four);
        return wrapperBentoSide3Four;
    }

    private LinearLayout getWrapperBentoSide4() {
        if (wrapperBentoSide4 == null)
            wrapperBentoSide4 = (LinearLayout) findViewById(R.id.btn_bento_side_4);
        return wrapperBentoSide4;
    }

    private LinearLayout getWrapperOaHeader() {
        if (wrapperOaHeader == null)
            wrapperOaHeader = (LinearLayout) findViewById(R.id.wrapper_oa_header);
        return wrapperOaHeader;
    }

    private SpinnerMaterial getSpinnerDate() {
        if (spinnerDate == null)
            spinnerDate = (SpinnerMaterial) findViewById(R.id.spinner_date);
        return spinnerDate;
    }

    private SpinnerMaterial getSpinnerTime() {
        if (spinnerTime == null)
            spinnerTime = (SpinnerMaterial) findViewById(R.id.spinner_time);
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


    private NextDayMainListAdapter getMainAdapter() {
        if (mainAdapter == null)
            mainAdapter = new NextDayMainListAdapter(BuildBentoActivity.this);
        return mainAdapter;
    }

    private NextDayMainListAdapter getSideAdapter() {
        if (sideAdapter == null)
            sideAdapter = new NextDayMainListAdapter(BuildBentoActivity.this);
        return sideAdapter;
    }

    private NextDayMainListAdapter getAddOnAdapter() {
        if (addOnAdapter == null)
            addOnAdapter = new NextDayMainListAdapter(BuildBentoActivity.this);
        return addOnAdapter;
    }

    private ListView getListMain() {
        if (mListMain == null)
            mListMain = (ListView) findViewById(R.id.list_next_main);
        return mListMain;
    }

    private GridView getGridSide() {
        if (gridSide == null)
            gridSide = (GridView) findViewById(R.id.grid_next_side);
        return gridSide;
    }

    private ListView getListAddOn() {
        if (mListAddOn == null)
            mListAddOn = (ListView) findViewById(R.id.list_next_add_on);
        return mListAddOn;
    }
}
