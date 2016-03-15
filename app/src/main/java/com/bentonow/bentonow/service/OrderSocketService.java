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

    private final WebSocketServiceBinder binder = new WebSocketServiceBinder();
    private Socket mSocket = null;
    private Handler mHandler;
    private Runnable mRunnable;
    private Runnable mRunnableOrderHistory;
    private boolean connecting = false;
    private boolean disconnectingPurposefully = false;
    private boolean bIsTransportError = false;
    private boolean bIsTransportClosed = false;
    private boolean mReconnecting = false;
    private String sTransportError = "";
    private String sTransportClosed = "";
    private String sToken = "";
    private String sDriverId = "";
    private Calendar mCalLoc;
    private OrderStatusListener mSocketListener;
    private OrderHistoryItemModel mOrder;


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
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (connecting)
                    checkServiceStatus();
            }
        };
        mRunnableOrderHistory = new Runnable() {
            @Override
            public void run() {
                if (mSocketListener != null)
                    mSocketListener.getOrderHistory();

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
                    String sPath = BentoNowApi.getOrderStatusNode(sUser, sPass);
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
                                    sTransportClosed = "";
                                    sTransportError = "";
                                    bIsTransportClosed = false;
                                    bIsTransportError = false;
                                    mReconnecting = false;

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

                mReconnecting = true;

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
                        sTransportError = headers.toString();
                        bIsTransportError = true;
                        DebugUtils.logError(TAG, "Transport.EVENT_ERROR: " + headers.toString());
                    }
                }).on(Transport.EVENT_CLOSE, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Map<String, List<String>> headers = (Map<String, List<String>>) args[0];
                        sTransportClosed = headers.toString();
                        bIsTransportClosed = true;
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
                        mCalLoc = Calendar.getInstance();
                        // mSocketListener.onDriverLocation(37.806698, -122.419831);
                        mSocketListener.onDriverLocation(Double.parseDouble(mGloc.getLat()), Double.parseDouble(mGloc.getLng()));
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

    public void trackDriver(final String sDriverId) {
        this.sDriverId = sDriverId;
        String sUrl = BentoNowApi.getDriverTrackUrl(sDriverId, mOrder.getOrderId());
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
                trackGloc(sDriverId);
            }
        });
    }

    public void trackGloc(String sDriverId) {
        String sUrl = BentoNowApi.getDriverTrackGloc(sDriverId, sToken);
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
                                // mSocketListener.onDriverLocation(37.806698, -122.419831);
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
        String sUrl = BentoNowApi.getDriverUntrack(sDriverId, mOrder.getOrderId());
        DebugUtils.logDebug(TAG, "Untrack:: " + sUrl);
        mSocket.emit("get", sUrl, new Ack() {
            @Override
            public void call(Object... args) {
                DebugUtils.logDebug(TAG, "Untrack: " + args[0].toString());
                disconnectWebSocket();
            }
        });
    }

    private void startDriverLocationTimer() {
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
        super.onDestroy();
    }

    // Called when all clients have disconnected
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
