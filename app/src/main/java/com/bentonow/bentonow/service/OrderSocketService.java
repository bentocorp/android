package com.bentonow.bentonow.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.bentonow.bentonow.Utils.DebugUtils;
import com.bentonow.bentonow.listener.OrderStatusListener;
import com.bentonow.bentonow.model.order.history.OrderHistoryItemModel;
import com.bentonow.bentonow.model.socket.GlocSocketModel;
import com.bentonow.bentonow.model.socket.ResponseSocketModel;
import com.bentonow.bentonow.parse.SocketResponseParser;
import com.bentonow.bentonow.web.BentoNowApi;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;

public class OrderSocketService extends Service {

    public static final String TAG = "OrderSocketService";

    //TODO Return to false
    public static final boolean bIsProductionTesting = false;

    private final WebSocketServiceBinder binder = new WebSocketServiceBinder();
    private Socket mSocket = null;
    private Handler mHandler;
    private Runnable mRunnable;
    private Runnable mRunnableOrderHistory;
    private boolean connecting = false;
    private boolean disconnectingPurposefully = false;
    private String sToken = "";
    private Calendar mCalLoc;
    private OrderStatusListener mSocketListener;
    private OrderHistoryItemModel mOrder;
    private boolean bIsSocketAlive;


    private TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    }};

    @Override
    public void onCreate() {
        DebugUtils.logDebug(TAG, "Creating new OrderSocketService");
        mCalLoc = Calendar.getInstance();
        mHandler = new Handler();
        bIsSocketAlive = true;

        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (connecting && bIsSocketAlive)
                    checkServiceStatus();
            }
        };
        mRunnableOrderHistory = new Runnable() {
            @Override
            public void run() {
                if (mSocketListener != null)
                    mSocketListener.getOrderHistory();

                if (bIsSocketAlive)
                    checkOrderHistoryStatus();
            }
        };
    }

    public void connectWebSocket(String username, String password) {
        if (connecting) {
            DebugUtils.logDebug(TAG, "Connection in progress");
        } else if (mSocket != null && mSocket.connected()) {
            DebugUtils.logDebug(TAG, "Already connected");
        } else {
            try {
                connecting = true;

                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());
                IO.setDefaultSSLContext(sc);
                IO.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

                IO.Options opts = new IO.Options();

                opts.port = 8443;
                opts.forceNew = false;
                opts.reconnection = true;
                opts.secure = true;
                opts.sslContext = sc;
                opts.hostnameVerifier = new RelaxedHostNameVerifier();
                opts.reconnectionDelay = 500;
                opts.reconnectionDelayMax = 1000;
                opts.timeout = 5000;

                mSocket = IO.socket(BentoNowApi.getOrderStatusNode(), opts);
                socketAuthenticate(username, password);
                mSocket.connect();
            } catch (Exception e) {
                DebugUtils.logError(TAG, "connectWebSocket: " + e.toString());
                connecting = false;
                if (mSocketListener != null)
                    mSocketListener.onConnectionError("connectWebSocket: " + e.getMessage());
            }
        }
    }

    public void socketAuthenticate(final String sUser, final String sPass) {
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                try {
                    String sPath = bIsProductionTesting ?
                            BentoNowApi.getOrderStatusNode("kokushos@gmail.com", "$2y$10$SjHiW26uzryMQird4XVMIOmWI/Fa.nXhpcD3cBINGxgnJ36M3qtYW") : BentoNowApi.getOrderStatusNode(sUser, sPass);
                    DebugUtils.logDebug(TAG, "Connecting: " + sPath);
                    mSocket.emit("get", sPath, new Ack() {
                        @Override
                        public void call(Object[] args) {
                            try {
                                DebugUtils.logDebug(TAG, "socketAuthenticate: " + args[0].toString());
                                ResponseSocketModel mResponseSocket = SocketResponseParser.parseResponse(args[0].toString());

                                if (mResponseSocket.getCode() != 0) {
                                    if (mSocketListener != null)
                                        mSocketListener.onAuthenticationFailure(mResponseSocket.getMsg());
                                    DebugUtils.logError(TAG, "socketAuthenticate: " + mResponseSocket.getMsg());
                                    mSocket.disconnect();
                                } else {
                                    sToken = SocketResponseParser.parseToken(mResponseSocket.getRet());

                                    if (mSocketListener != null && !sToken.isEmpty())
                                        mSocketListener.onAuthenticationSuccess();

                                    startDriverLocationTimer();
                                    checkOrderHistoryStatus();

                                }
                            } catch (Exception e) {
                                if (mSocketListener != null)
                                    mSocketListener.onAuthenticationFailure(e.getMessage());
                                DebugUtils.logError(TAG, "SocketAuthenticate: " + e.toString());
                                mSocket.disconnect();
                            }
                        }
                    });
                } catch (Exception e) {
                    if (mSocketListener != null)
                        mSocketListener.onConnectionError("Connection Error: " + e.getMessage());
                    mSocket.disconnect();
                }
            }
        });
        mSocket.on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                for (Object mObject : args)
                    DebugUtils.logDebug(TAG, "EVENT_RECONNECT: " + mObject.toString());


            }
        });
        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                for (Object mObject : args) {
                    DebugUtils.logDebug(TAG, "EVENT_DISCONNECT: " + mObject.toString());

                    if (mObject.toString().contains("disconnect"))
                        mSocket.connect();
                }

                if (mSocketListener != null)
                    mSocketListener.onDisconnect(disconnectingPurposefully);

                disconnectingPurposefully = false;

            }
        });
        mSocket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                for (Object mObject : args)
                    DebugUtils.logDebug(TAG, "EVENT_ERROR: " + mObject.toString());


            }
        });
        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                for (Object mObject : args)
                    DebugUtils.logDebug(TAG, "EVENT_CONNECT_ERROR: " + mObject.toString());

                if (mSocketListener != null)
                    mSocketListener.onDisconnect(false);


            }
        });
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                for (Object mObject : args)
                    DebugUtils.logDebug(TAG, "EVENT_CONNECT_TIMEOUT: " + mObject.toString());


            }
        });
        mSocket.on(Socket.EVENT_RECONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                for (Object mObject : args)
                    DebugUtils.logDebug(TAG, "EVENT_RECONNECT_ERROR: " + mObject.toString());

            }
        });
        mSocket.on(Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                for (Object mObject : args)
                    DebugUtils.logDebug(TAG, "EVENT_RECONNECT_FAILED: " + mObject.toString());

            }
        });
        mSocket.on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
            @Override
            public void call(Object[] args) {
                for (Object mObject : args)
                    DebugUtils.logDebug(TAG, "EVENT_RECONNECTING: " + mObject.toString());

                if (mSocketListener != null)
                    mSocketListener.onReconnecting();

            }
        });
        mSocket.on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Transport transport = (Transport) args[0];

                transport.on(Transport.EVENT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Map<String, List<String>> headers = (Map<String, List<String>>) args[0];
                        DebugUtils.logError(TAG, "Transport.EVENT_ERROR: " + headers.toString());
                    }
                }).on(Transport.EVENT_CLOSE, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Map<String, List<String>> headers = (Map<String, List<String>>) args[0];
                        DebugUtils.logError(TAG, "Transport.EVENT_CLOSE: " + headers.toString());
                    }
                });
            }
        });
    }

    public void onNodeEventListener() {
        if (mSocket != null) {
            removeNodeListener();

            DebugUtils.logDebug(TAG, "Push: Subscribed");
            mSocket.on("push", new Emitter.Listener() {
                @Override
                public void call(Object[] args) {
                    try {
                        DebugUtils.logDebug(TAG, "Push: " + args[0].toString());
                        mSocketListener.onPush(args[0].toString());
                    } catch (Exception e) {
                        DebugUtils.logError(TAG, "Push: " + e.toString());
                    }
                }
            });

            DebugUtils.logDebug(TAG, "Loc: Subscribed");
            mSocket.on("loc", new Emitter.Listener() {
                @Override
                public void call(Object[] args) {
                    try {
                        DebugUtils.logDebug(TAG, "Loc: " + args[0].toString());
                        GlocSocketModel mGloc = SocketResponseParser.parseGloc(args[0].toString());
                        if (mGloc.getClientId().equals(mOrder.getDriverId())) {
                            mCalLoc = Calendar.getInstance();
                            mSocketListener.onDriverLocation(Double.parseDouble(mGloc.getLat()), Double.parseDouble(mGloc.getLng()));
                        } else
                            unTrackDriver(mGloc.getClientId());
                    } catch (Exception e) {
                        DebugUtils.logError(TAG, "Loc: " + e.toString());
                    }

                }
            });
        }
    }

    public void removeNodeListener() {
        if (mSocket != null) {
            mSocket.off("push");
            mSocket.off("pong");
        }

    }

    public void trackDriver() {
        String sUrl = bIsProductionTesting ? BentoNowApi.getDriverTrackUrl("64", "o-23341") : BentoNowApi.getDriverTrackUrl(mOrder.getDriverId(), mOrder.getOrderId());

        DebugUtils.logDebug(TAG, "TrackDriver:: " + sUrl);
        mSocket.emit("get", sUrl, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    ResponseSocketModel mResponse = SocketResponseParser.parseResponse(args[0].toString());

                    switch (mResponse.getCode()) {
                        case 0:
                            onNodeEventListener();
                            break;
                        default:
                            DebugUtils.logError(TAG, "Track: " + args[0].toString());
                            break;
                    }

                } catch (Exception e) {
                    DebugUtils.logError(TAG, "Track: " + e.toString());
                }

                trackGloc();
            }
        });

    }

    public void trackGloc() {
        String sUrl = bIsProductionTesting ? BentoNowApi.getDriverTrackGloc("64", sToken) : BentoNowApi.getDriverTrackGloc(mOrder.getDriverId(), sToken);
        DebugUtils.logDebug(TAG, "TrackGloc:: " + sUrl);
        mSocket.emit("get", sUrl, new Ack() {
            @Override
            public void call(Object... args) {
                try {
                    DebugUtils.logDebug(TAG, "Get Track Gloc");
                    ResponseSocketModel mResponse = SocketResponseParser.parseResponse(args[0].toString());
                    switch (mResponse.getCode()) {
                        case 0:
                            GlocSocketModel mGloc = SocketResponseParser.parseGloc(mResponse.getRet());
                            if (mGloc != null) {
                                mCalLoc = Calendar.getInstance();
                                mSocketListener.trackDriverByGloc(Double.parseDouble(mGloc.getLat()), Double.parseDouble(mGloc.getLng()));
                            } else
                                DebugUtils.logError(TAG, "Gloc: " + args[0].toString());
                            break;
                        default:
                            DebugUtils.logError(TAG, "Gloc: " + args[0].toString());
                            break;
                    }

                } catch (Exception e) {
                    DebugUtils.logError(TAG, "Gloc: " + e.toString());
                }
            }
        });
    }

    public void untrack() {
        String sUrl = bIsProductionTesting ? BentoNowApi.getDriverUntrack("o-23341", "64") : BentoNowApi.getDriverUntrack(mOrder.getOrderId(), mOrder.getDriverId());
        DebugUtils.logDebug(TAG, "Untrack:: " + sUrl);
        mSocket.emit("get", sUrl, new Ack() {
            @Override
            public void call(Object... args) {
                DebugUtils.logDebug(TAG, "Untrack: " + args[0].toString());
            }
        });
        disconnectWebSocket();
    }

    private void unTrackDriver(String driverId) {
        String sUrl = BentoNowApi.getDriverUntrack(mOrder.getOrderId(), driverId);
        DebugUtils.logDebug(TAG, "Untrack:: " + sUrl);
        mSocket.emit("get", sUrl, new Ack() {
            @Override
            public void call(Object... args) {
                DebugUtils.logDebug(TAG, "Untrack: " + args[0].toString());
            }
        });
    }

    private void startDriverLocationTimer() {
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 1000 * 60);
    }

    public void checkServiceStatus() {
        if (mCalLoc == null)
            mCalLoc = Calendar.getInstance();

        Calendar mCalNow = Calendar.getInstance();
        long lSeconds = (mCalNow.getTimeInMillis() - mCalLoc.getTimeInMillis()) / 1000;

        if (lSeconds > 5 && !sToken.isEmpty()) {
            mCalLoc = Calendar.getInstance();
            DebugUtils.logDebug(TAG, "Exception after: " + lSeconds + " seconds.");
            mSocketListener.trackDriverByGoogleMaps();
        }

        startDriverLocationTimer();
    }

    private void checkOrderHistoryStatus() {
        mHandler.postDelayed(mRunnableOrderHistory, 30000);
    }

    public void setWebSocketLister(OrderStatusListener mListener) {
        mSocketListener = mListener;
    }

    public boolean isSocketListener() {
        return mSocketListener != null;
    }

    public void disconnectWebSocket() {
        disconnectingPurposefully = true;
        connecting = false;
        mHandler.removeCallbacks(mRunnable);
        mHandler.removeCallbacks(mRunnableOrderHistory);

        if (mSocket != null) {
            DebugUtils.logDebug(TAG, "disconnecting");
            removeNodeListener();
            mSocket.off();
            mSocket.disconnect();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        DebugUtils.logDebug(TAG, "destroying OrderSocketService");
        untrack();
        bIsSocketAlive = false;
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mSocket == null || !mSocket.connected()) {
            stopSelf();
        }
        return true;
    }

    public void setTrackingOrder(OrderHistoryItemModel mOrder) {
        this.mOrder = mOrder;
    }

    public static class RelaxedHostNameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public class WebSocketServiceBinder extends Binder {
        public OrderSocketService getService() {
            return OrderSocketService.this;
        }
    }

}
