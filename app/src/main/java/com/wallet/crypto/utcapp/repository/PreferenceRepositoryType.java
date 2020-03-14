package com.wallet.crypto.utcapp.repository;

import com.wallet.crypto.utcapp.entity.GasSettings;
import com.wallet.crypto.utcapp.entity.Wallet;

public interface PreferenceRepositoryType {
	String getCurrentWalletAddress();
	void setCurrentWalletAddress(String address);
	String getDefaultDataByType(String type);
	void setDefaultDataByType(String type,String data);
	void setIsSkipBackUp(Wallet wallet,boolean isSkip);
	String getDefaultNetwork();
	void setDefaultNetwork(String netName);
	boolean getIsSkipBackUp(Wallet wallet);
	GasSettings getGasSettings(boolean forTokenTransfer);
	GasSettings getGasSettingsByGasType(String gasType);
	void setGasSettings(GasSettings gasPrice);
	String getSearchEngine();
	void setSearchEngine(final String p0);
}
