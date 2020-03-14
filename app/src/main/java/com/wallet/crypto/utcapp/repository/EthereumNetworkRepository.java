package com.wallet.crypto.utcapp.repository;

import android.text.TextUtils;

import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.Ticker;
import com.wallet.crypto.utcapp.service.TickerService;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.Single;

import static com.wallet.crypto.utcapp.C.ETFP_NETWORK_NAME;
import static com.wallet.crypto.utcapp.C.ETHEREUM_NETWORK_NAME;
import static com.wallet.crypto.utcapp.C.ETH_SYMBOL;
import static com.wallet.crypto.utcapp.C.KOVAN_NETWORK_NAME;
import static com.wallet.crypto.utcapp.C.POA_NETWORK_NAME;
import static com.wallet.crypto.utcapp.C.POA_SYMBOL;
import static com.wallet.crypto.utcapp.C.ROPSTEN_NETWORK_NAME;
import static com.wallet.crypto.utcapp.C.CLASSIC_NETWORK_NAME;
import static com.wallet.crypto.utcapp.C.ETC_SYMBOL;
import static com.wallet.crypto.utcapp.C.TAIJICHAIN_NETWORK_NAME;
import static com.wallet.crypto.utcapp.C.OCC_SYMBOL;


public class EthereumNetworkRepository implements EthereumNetworkRepositoryType {

	/*
	 * 定期获取价格的地址需要在以下类里面的变量进行设置
	 * com.wallet.crypto.utcapp.service.TrustWalletTickerService
	 */
	//private static final String TRUST_API_URL = "http://occ.coinpool.by";

	private final NetworkInfo[] NETWORKS = new NetworkInfo[]{
//			new NetworkInfo(ETFP_NETWORK_NAME, OCC_SYMBOL,
//					"http://rpc.mbbsystem.com:50505",
//					//"https://app.taijiblockchain.net",
//					"http://wallet.mbbsystem.com/",2,
//					"http://explorer.mbbsystem.com/", 100, true),

			new NetworkInfo(ETHEREUM_NETWORK_NAME, ETH_SYMBOL,
					"https://mainnet.infura.io/llyrtzQ3YhkdESt2Fzrk",
					"https://api.trustwalletapp.com/",1,
					"https://etherscan.io/", 1, true),
//			new NetworkInfo(CLASSIC_NETWORK_NAME, ETC_SYMBOL,
//					"https://mewapi.epool.io/",
//					"https://classic.trustwalletapp.com",1,
//					"https://gastracker.io", 61, true),
//			new NetworkInfo(POA_NETWORK_NAME, POA_SYMBOL,
//					"https://core.poa.network",
//					"https://poa.trustwalletapp.com", 1,"poa", 99, false),
//			new NetworkInfo(KOVAN_NETWORK_NAME, ETH_SYMBOL,
//					"https://kovan.infura.io/llyrtzQ3YhkdESt2Fzrk",
//					"https://kovan.trustwalletapp.com/",1,
//					"https://kovan.etherscan.io", 42, false),
//			new NetworkInfo(ROPSTEN_NETWORK_NAME, ETH_SYMBOL,
//					"https://ropsten.infura.io/llyrtzQ3YhkdESt2Fzrk",
//					"https://ropsten.trustwalletapp.com/",1,
//					"https://ropsten.etherscan.io", 3, false),
	};
	public static  String getExplorerScanUrl(String name) {
		String url="http://explorer.taijiblockchain.net";
		switch (name) {
			case TAIJICHAIN_NETWORK_NAME:
				url= "http://explorer.taijiblockchain.net";
				break;
			case ETHEREUM_NETWORK_NAME:
				url= "https://etherscan.io/";
				break;
			case CLASSIC_NETWORK_NAME:
				url= "https://gastracker.io";
				break;
			case POA_NETWORK_NAME:
				url= "https://poa.trustwalletapp.com";
				break;
			case KOVAN_NETWORK_NAME:
				url= "https://kovan.etherscan.io";
				break;
			case ROPSTEN_NETWORK_NAME:
				url= "https://ropsten.etherscan.io";
				break;
		}
		return url;
	}
	private final PreferenceRepositoryType preferences;
	private final TickerService tickerService;
	private NetworkInfo defaultNetwork;
	private final Set<OnNetworkChangeListener> onNetworkChangedListeners = new HashSet<>();

	public EthereumNetworkRepository(PreferenceRepositoryType preferenceRepository, TickerService tickerService) {
		this.preferences = preferenceRepository;
		this.tickerService = tickerService;
		defaultNetwork = getByName(preferences.getDefaultNetwork());
		if (defaultNetwork == null) {
			defaultNetwork = NETWORKS[0];
		}
	}

	private NetworkInfo getByName(String name) {
		if (!TextUtils.isEmpty(name)) {
			for (NetworkInfo NETWORK : NETWORKS) {
				if (name.equals(NETWORK.name)) {
					return NETWORK;
				}
			}
		}
		return null;
	}

	@Override
	public NetworkInfo getDefaultNetwork() {
		return defaultNetwork;
	}

	@Override
	public void setDefaultNetworkInfo(NetworkInfo networkInfo) {
		defaultNetwork = networkInfo;
		preferences.setDefaultNetwork(defaultNetwork.name);

		for (OnNetworkChangeListener listener : onNetworkChangedListeners) {
			listener.onNetworkChanged(networkInfo);
		}
	}

	@Override
	public NetworkInfo[] getAvailableNetworkList() {
		return NETWORKS;
	}

	@Override
	public void addOnChangeDefaultNetwork(OnNetworkChangeListener onNetworkChanged) {
		onNetworkChangedListeners.add(onNetworkChanged);
	}

	@Override
	public Single<Ticker> getTicker() {
		return Single.fromObservable(tickerService
				.fetchTickerPrice(getDefaultNetwork().symbol));
	}
}
