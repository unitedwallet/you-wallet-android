package com.wallet.crypto.utcapp.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.wallet.crypto.utcapp.C;
import com.wallet.crypto.utcapp.entity.GasSettings;
import com.wallet.crypto.utcapp.entity.Wallet;

import java.math.BigInteger;

public class SharedPreferenceRepository implements PreferenceRepositoryType {

	private static final String CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address";
	private static final String DEFAULT_NETWORK_NAME_KEY = "default_network_name";
	private static final String GAS_PRICE_KEY  ="gas_price";
	private static final String GAS_LIMIT_KEY  ="gas_limit";
	private static final String GAS_LIMIT_FOR_TOKENS_KEY = "gas_limit_for_tokens";
	private static final String SEARCH_ENGINE_KEY = "search_engine";
	private final SharedPreferences pref;

	public SharedPreferenceRepository(Context context) {
		pref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public String getCurrentWalletAddress() {
		return pref.getString(CURRENT_ACCOUNT_ADDRESS_KEY, null);
	}

	@Override
	public void setCurrentWalletAddress(String address) {
		Log.d("地址2：L",address);
		pref.edit().putString(CURRENT_ACCOUNT_ADDRESS_KEY, address).apply();
	}

	@Override
	public String getDefaultDataByType(String type) {
		return pref.getString(type, null);
	}

	@Override
	public void setDefaultDataByType(String type,String data) {
		pref.edit().putString(type, data).apply();
	}

	@Override
	public void setIsSkipBackUp(Wallet wallet,boolean isSkip) {
		pref.edit().putBoolean(wallet.address+"_isSkipBackUp", isSkip).apply();
	}
	@Override
	public boolean getIsSkipBackUp(Wallet wallet) {
		return pref.getBoolean(wallet.address+"_isSkipBackUp",true);
	}
	@Override
	public String getDefaultNetwork() {
		return pref.getString(DEFAULT_NETWORK_NAME_KEY, null);
	}

	@Override
	public void setDefaultNetwork(String netName) {
		pref.edit().putString(DEFAULT_NETWORK_NAME_KEY, netName).apply();
	}

	@Override
	public GasSettings getGasSettings(boolean forTokenTransfer) {
		BigInteger gasPrice = new BigInteger(pref.getString(GAS_PRICE_KEY, C.DEFAULT_GAS_PRICE));
		//BigInteger gasLimit = new BigInteger(pref.getString(GAS_LIMIT_KEY, C.DEFAULT_GAS_LIMIT));
		BigInteger gasLimit = new BigInteger(C.DEFAULT_MAIN_GAS_LIMIT);
		if (forTokenTransfer) {
			gasLimit = new BigInteger(pref.getString(GAS_LIMIT_FOR_TOKENS_KEY, C.DEFAULT_TOKENS_GAS_LIMIT));
		}

		return new GasSettings(gasPrice, gasLimit);
	}
	@Override
	public GasSettings getGasSettingsByGasType(String gasType) {
		BigInteger gasPrice = new BigInteger(pref.getString(GAS_PRICE_KEY, C.DEFAULT_GAS_PRICE));
		//BigInteger gasLimit = new BigInteger(pref.getString(GAS_LIMIT_KEY, C.DEFAULT_GAS_LIMIT));
		BigInteger gasLimit = new BigInteger(C.DEFAULT_MAIN_GAS_LIMIT);
		if(!TextUtils.isEmpty(gasType)){
			if(gasType.equals(C.DEFAULT_MAIN_GAS_TYPE))
				gasLimit = new BigInteger(C.DEFAULT_MAIN_GAS_LIMIT);
			if(gasType.equals(C.DEFAULT_TOKEN_GAS_TYPE))
				gasLimit = new BigInteger(C.DEFAULT_TOKENS_GAS_LIMIT);
			if(gasType.equals(C.DEFAULT_CREATE_TOKEN_GAS_TYPE))
				gasLimit = new BigInteger(C.DEFAULT_CREATE_TOKEN_GAS_LIMIT);
		}

		return new GasSettings(gasPrice, gasLimit);
	}
	@Override
	public void setGasSettings(GasSettings gasSettings) {
		pref.edit().putString(GAS_PRICE_KEY, gasSettings.gasPrice.toString()).apply();
		pref.edit().putString(GAS_LIMIT_KEY, gasSettings.gasLimit.toString()).apply();
	}
	@Override
	public String getSearchEngine() {
		return this.pref.getString(SEARCH_ENGINE_KEY, "Google");
	}
	@Override
	public void setSearchEngine(final String s) {
		this.pref.edit().putString(SEARCH_ENGINE_KEY, s).apply();
	}

}
