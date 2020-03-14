package com.wallet.crypto.utcapp.repository;

import com.wallet.crypto.utcapp.entity.NetworkInfo;

public interface OnNetworkChangeListener {
	void onNetworkChanged(NetworkInfo networkInfo);
}
