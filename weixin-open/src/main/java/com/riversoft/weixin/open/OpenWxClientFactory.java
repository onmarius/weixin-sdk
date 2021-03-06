package com.riversoft.weixin.open;

import com.google.common.cache.CacheBuilder;
import com.riversoft.weixin.common.AccessTokenHolder;
import com.riversoft.weixin.common.DefaultAccessTokenHolder;
import com.riversoft.weixin.common.WxClient;
import com.riversoft.weixin.open.base.AppSetting;
import com.riversoft.weixin.open.base.WxEndpoint;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by exizhai on 11/12/2015.
 */
public class OpenWxClientFactory {

    private static OpenWxClientFactory instance = null;
    private static ConcurrentMap<String, WxClient> wxClients = CacheBuilder.newBuilder().initialCapacity(8)
            .maximumSize(128).<String, WxClient>build().asMap();

    private OpenWxClientFactory() {
    }

    public synchronized static OpenWxClientFactory getInstance() {
        if (instance == null) {
            instance = new OpenWxClientFactory();
        }
        return instance;
    }

    public WxClient defaultWxClient() {
        return with(AppSetting.defaultSettings());
    }

    public WxClient with(AppSetting appSetting) {
        if (!wxClients.containsKey(key(appSetting))) {
            String url = WxEndpoint.get("url.token.get");
            String clazz = appSetting.getTokenHolderClass();

            AccessTokenHolder accessTokenHolder = null;
            if(clazz == null || "".equals(clazz)) {
                accessTokenHolder = new DefaultAccessTokenHolder(url, appSetting.getAppId(), appSetting.getSecret());
            } else {
                try {
                    accessTokenHolder = (AccessTokenHolder)Class.forName(clazz).newInstance();
                    accessTokenHolder.setClientId(appSetting.getAppId());
                    accessTokenHolder.setClientSecret(appSetting.getSecret());
                    accessTokenHolder.setTokenUrl(url);
                } catch (Exception e) {
                    accessTokenHolder = new DefaultAccessTokenHolder(url, appSetting.getAppId(), appSetting.getSecret());
                }
            }

            WxClient wxClient = new WxClient(appSetting.getAppId(), appSetting.getSecret(), accessTokenHolder);
            wxClients.putIfAbsent(key(appSetting), wxClient);
        }

        return wxClients.get(key(appSetting));
    }

    private String key(AppSetting appSetting) {
        return appSetting.getAppId() + ":" + appSetting.getSecret();
    }
}

