package com.wallet.crypto.utcapp.repository;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.TokenInfo;
import com.wallet.crypto.utcapp.entity.Transaction;
import com.wallet.crypto.utcapp.entity.TransactionContract;
import com.wallet.crypto.utcapp.entity.TransactionOperation;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.entity.Contract;
import com.wallet.crypto.utcapp.repository.entity.RealmTokenInfo;
import com.wallet.crypto.utcapp.repository.entity.RealmTransaction;
import com.wallet.crypto.utcapp.repository.entity.RealmTransactionOperation;
import com.wallet.crypto.utcapp.util.RealmHelper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.Sort;

import static java.math.BigDecimal.ROUND_DOWN;
import static java.math.BigDecimal.ROUND_UP;

public class RealmTransactionSource implements TransactionLocalSource {
    @Override
    public Single<Transaction[]> fetchTransaction(NetworkInfo networkInfo, Wallet wallet) {
        return Single.fromCallable(() -> {
            Realm realm = null;
            try {
                realm = RealmHelper.getInstance().getRealmInstance(networkInfo, wallet);
                RealmResults<RealmTransaction> realmItems = realm.where(RealmTransaction.class)
                        .sort("timeStamp", Sort.DESCENDING)
                        .findAll();
                int len = realmItems.size();
                Transaction[] result = new Transaction[len];
                for (int i = 0; i < len; i++) {
                    RealmTransaction realmItem = realmItems.get(i);
                    if (realmItem != null) {
                        TransactionOperation[] transactionOperations = null;
                        if (realmItem.getOperations() != null && realmItem.getOperations().size() > 0) {
                            transactionOperations = new TransactionOperation[realmItem.getOperations().size()];
                            for (int j = 0; j < realmItem.getOperations().size(); j++) {
                                RealmTransactionOperation realmTransactionOperation = realmItem.getOperations().get(j);
                                if (realmTransactionOperation != null) {
                                    TransactionContract transactionContract = new TransactionContract(
                                            realmTransactionOperation.getContract_address(),
                                            realmTransactionOperation.getContract_name(),
                                            realmTransactionOperation.getContract_totalSupply(),
                                            realmTransactionOperation.getContract_decimals(),
                                            realmTransactionOperation.getContract_symbol()
                                    );
                                    TransactionOperation transactionOperation = new TransactionOperation(
                                            realmTransactionOperation.getTransactionId(),
                                            realmTransactionOperation.getViewType(),
                                            realmTransactionOperation.getFrom(),
                                            realmTransactionOperation.getTo(),
                                            realmTransactionOperation.getValue(),
                                            transactionContract);
                                    transactionOperations[j] = transactionOperation;
                                }
                            }
                        }
                        result[i] = new Transaction(
                                realmItem.getHash(),
                                realmItem.getError(),
                                realmItem.getBlockNumber(),
                                realmItem.getTimeStamp(),
                                realmItem.getNonce(),
                                realmItem.getFrom(),
                                realmItem.getTo(),
                                realmItem.getValue(),
                                realmItem.getGas(),
                                realmItem.getGasPrice(),
                                realmItem.getInput(),
                                realmItem.getGasUsed(),
                                transactionOperations,
                                realmItem.getContract()
                        );
                    }
                }
              /*  if (System.currentTimeMillis() - lastGetTimeFromNetWork > MAX_TIME_OUT) {
                    return new Transaction[0];
                } else {
                    return result;
                }*/
                return result;
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
        });
    }
    @Override
    public void deleteTransactionbs(NetworkInfo networkInfo, Wallet wallet) {
        Realm realm = null;
        try {
            realm = RealmHelper.getInstance().getRealmInstance(networkInfo, wallet);
            RealmResults<RealmTransaction> realmItem = realm.where(RealmTransaction.class).notEqualTo("error", "pending").findAll();
            realm.executeTransaction(r -> {
                    realmItem.deleteAllFromRealm();
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lastGetTimeFromNetWork = System.currentTimeMillis();
            if (realm != null) {
                realm.close();
            }
        }
    }

    private static long lastGetTimeFromNetWork = 0;
    private static final long MAX_TIME_OUT = DateUtils.MINUTE_IN_MILLIS;

    @Override
    public void putTransactions(NetworkInfo networkInfo, Wallet wallet, Transaction[] transactions) {
        Realm realm = null;
        try {
            realm = RealmHelper.getInstance().getRealmInstance(networkInfo, wallet);
            RealmResults<RealmTransaction> realmItem = realm.where(RealmTransaction.class).findAll();

            realm.executeTransaction(r -> {
                if (transactions != null && transactions.length > 0) {
                    Map<String, String> stringStringMap = new HashMap<String, String>();
                    for (Transaction transaction : transactions) {
                        RealmTransaction realmTransaction = new RealmTransaction();
                        realmTransaction.setBlockNumber(transaction.blockNumber);
                        realmTransaction.setContract(transaction.contract);
                        realmTransaction.setError(transaction.error);
                        realmTransaction.setFrom(transaction.from);
                        realmTransaction.setGas(transaction.gas);
                        realmTransaction.setGasPrice(transaction.gasPrice);
                        realmTransaction.setGasUsed(transaction.gasUsed);
                        realmTransaction.setHash(transaction.hash);
                        realmTransaction.setInput(transaction.input);
                        realmTransaction.setNonce(transaction.nonce);
                        realmTransaction.setOperations(transaction.operations);
                        realmTransaction.setTimeStamp(transaction.timeStamp);
                        realmTransaction.setTo(transaction.to);
                        realmTransaction.setValue(transaction.value);
                        r.insertOrUpdate(realmTransaction);
                        stringStringMap.put(transaction.hash, transaction.hash);
                    }
                   /* if (transactions.length < 1000) {//增加分页功能暂时无法处理失效的交易记录
                        for (RealmTransaction realmTransaction : realmItem) {
                            if (!stringStringMap.containsKey(realmTransaction.getHash())) {
                                if (TextUtils.isEmpty(realmTransaction.getError())||!realmTransaction.getError().equals("pending")) {
                                    realmTransaction.setError("Invalid");
                                    r.insertOrUpdate(realmTransaction);
                                }
                            }

                        }
                    }*/
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lastGetTimeFromNetWork = System.currentTimeMillis();
            if (realm != null) {
                realm.close();
            }
        }
    }

    public boolean isHasSameNoncePending(NetworkInfo networkInfo, Wallet wallet, int nonce) {
        Realm realm = null;
        boolean isHas = false;
        try {
            realm = RealmHelper.getInstance().getRealmInstance(networkInfo, wallet);
            RealmTransaction realmItem = realm.where(RealmTransaction.class).equalTo("nonce", nonce).equalTo("error", "pending")
                    .findFirst();
            if (realmItem != null) {
                isHas = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }
            return isHas;
        }

    }

    public boolean isGasAddCheckPending(NetworkInfo networkInfo, Wallet wallet, int nonce, String gas) {
        Realm realm = null;
        boolean isHas = false;

        try {
            realm = RealmHelper.getInstance().getRealmInstance(networkInfo, wallet);
            RealmTransaction realmItem = realm.where(RealmTransaction.class).equalTo("nonce", nonce).equalTo("error", "pending")
                    .findFirst();
            if (realmItem != null) {
                String gasLocal = realmItem.getGasPrice();
                Log.d("gas", gas + "   " + gasLocal);
                BigDecimal gasLocalbigDecimal = new BigDecimal(gasLocal);
                BigDecimal gasbigDecimal = new BigDecimal(gas);
                if (gasbigDecimal.divide(gasLocalbigDecimal, 2, ROUND_UP).compareTo(new BigDecimal("1.1")) > 0) {
                    isHas = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }
            return isHas;
        }

    }

    public boolean putOneTransaction(NetworkInfo networkInfo, Wallet wallet, Transaction transaction) {
        Realm realm = null;
        final boolean[] isOverLastPendingTransaction = {false};
        try {

            realm = RealmHelper.getInstance().getRealmInstance(networkInfo, wallet);

            realm.executeTransaction(r -> {
                boolean isHasSame = false;
                RealmTransaction realmItem = r.where(RealmTransaction.class).equalTo("nonce", transaction.nonce).equalTo("error", "pending")
                        .findFirst();
                if (realmItem != null) {
                    realmItem.deleteFromRealm();
                    isHasSame = true;
                }
                if (transaction != null) {
                    RealmTransaction realmTransaction = new RealmTransaction();
                    realmTransaction.setBlockNumber(transaction.blockNumber);
                    realmTransaction.setContract(transaction.contract);
                    realmTransaction.setError(transaction.error);
                    realmTransaction.setFrom(transaction.from);
                    realmTransaction.setGas(transaction.gas);
                    realmTransaction.setGasPrice(transaction.gasPrice);
                    realmTransaction.setGasUsed(transaction.gasUsed);
                    realmTransaction.setHash(transaction.hash);
                    realmTransaction.setInput(transaction.input);
                    realmTransaction.setNonce(transaction.nonce);
                    realmTransaction.setOperations(transaction.operations);
                    realmTransaction.setTimeStamp(transaction.timeStamp);
                    realmTransaction.setTo(transaction.to);
                    realmTransaction.setValue(transaction.value);
                    r.insertOrUpdate(realmTransaction);
                    if (isHasSame) isOverLastPendingTransaction[0] = true;
                }
            });
            return isOverLastPendingTransaction[0];
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lastGetTimeFromNetWork = System.currentTimeMillis();
            if (realm != null) {
                realm.close();
            }

        }
        return isOverLastPendingTransaction[0];
    }

    @Override
    public void clear() {

    }

}
