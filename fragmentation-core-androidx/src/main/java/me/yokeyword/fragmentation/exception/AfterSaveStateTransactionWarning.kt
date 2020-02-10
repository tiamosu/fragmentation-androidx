package me.yokeyword.fragmentation.exception

import android.util.Log

/**
 * Perform the transaction action after onSaveInstanceState.
 *
 * This is dangerous because the action can
 * be lost if the activity needs to later be restored from its state.
 *
 *
 * If you don't want to lost the action:
 *
 *
 * //    // ReceiverActivity or Fragment:
 * //    void start() {
 * //        startActivityForResult(new Intent(this, SenderActivity.class), 100);
 * //    }
 * //
 * //    @Override
 * //    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 * //        super.onActivityResult(requestCode, resultCode, data);
 * //        if (requestCode == 100 && resultCode == 100) {
 * //            // begin transaction
 * //        }
 * //    }
 * //
 * //    // SenderActivity or Fragment:
 * //    void do(){ // Let ReceiverActivity（or Fragment）begin transaction
 * //        setResult(100);
 * //        finish();
 * //    }
 *
 *
 * Created by YoKey on 17/12/26.
 */
class AfterSaveStateTransactionWarning(action: String) : RuntimeException("Warning: Perform this $action action after onSaveInstanceState!") {

    init {
        Log.w("Fragmentation", message ?: "")
    }
}
