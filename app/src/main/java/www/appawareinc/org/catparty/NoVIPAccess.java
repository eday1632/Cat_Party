package www.appawareinc.org.catparty;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import inappbilling.IabResult;

public class NoVIPAccess extends Fragment {
    private OnFragmentInteractionListener mListener;
    private static ImageView imageView;
    private static Animation ropeFade;
    private static Context context;
    private static Button purchase;
    static final String SKU_VIPACCESS = "vip_access";
    private IabHelper mHelper;
    static final String ITEM_SKU = "android.test.purchased";

    public static NoVIPAccess newInstance (Context mContext){
        NoVIPAccess fragment = new NoVIPAccess();
        context = mContext;

        return fragment;
    }

    public static void hideViews(){
        imageView.setVisibility(View.INVISIBLE);
        purchase.setVisibility(View.INVISIBLE);
    }

    public static void showViews(){
        imageView.setVisibility(View.VISIBLE);
        purchase.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.no_vip_access, container, false);
        imageView = (ImageView) rootView.findViewById(R.id.bouncer);
        ropeFade = AnimationUtils.loadAnimation(context, R.anim.rope_fade);
        SharedPreferences prefs = context.getSharedPreferences("vip_access", 0);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("granted", 1);
        editor.apply();

        purchase = (Button) rootView.findViewById(R.id.purchase);
        purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartForVIPAccess();
                //onUpgradeAppButtonClicked();
                editor.putInt("granted", 2);//1 for testing, 2 for production
                editor.commit();
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.restart_dialog);
                dialog.getWindow().setLayout(Math.round(280 * TwoRooms.densityMultiple),
                        Math.round(244 * TwoRooms.densityMultiple)); //width, height

                Button yesDelete = (Button) dialog.findViewById(R.id.buttonOkay);
                yesDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        restartForVIPAccess();
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        String base64EncodedPublicKey =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAov9+84dDesqC622L6XpYrGfXfqT399jNTY2wWNE4tDCxE/n9KfEDAByw3J39fXrJfQBGrPY4JYOj2JIxS6hcwnouKdWTcd2f4WN2+QOD2E1WSitDbxmYyZa2A2CsBLliCIrwAH6ylrHzRMyogxqIvE/HCaEju4LvsD72pB+wfsLRHCuFI53HYYVp/5scDBNHd+bMup1VJvwl5OAAs8Hz+d1ExFS+wJg5lBwM5eYj1FZtRpM13IBBkkK5qQbAG3VEZB62QfDJXuvHbsTholb1a141rmDXn08+23rrNPSPirMS+/sTyGZzI2H8OSzvB3FY3M1FS+PzMBFCLPC0jkACmwIDAQAB";

//        BuildKey bk = new BuildKey();
//        String pubKey = bk.getKey();
        mHelper = new IabHelper(context, base64EncodedPublicKey);
//        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() { //TODO: comment out when using emulator
//            @Override
//            public void onIabSetupFinished(IabResult result) {
//                if (!result.isSuccess()) {
//                    Log.d("InAppBilling", "In-app Billing setup failed: " +
//                            result);
//                } else {
//                    Log.d("InAppBilling", "In-app Billing is set up OK");
//                    mHelper.queryInventoryAsync(mReceivedInventoryListener);
//                }
//            }
//        });

        return rootView;
    }

    public void restartForVIPAccess(){
//        mHelper.launchPurchaseFlow((Activity)context, ITEM_SKU, 10001, mPurchaseFinishedListener, "mypurchasetoken");
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage( context.getPackageName() );

        PendingIntent pending = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + 300, pending);
        System.exit(2);
    }

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                // Handle failure
            } else {
                Purchase donation = inventory.getPurchase(ITEM_SKU);
                if (donation != null && verifyDeveloperPayload(donation)) {
                    mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                            mConsumeFinishedListener);
                }
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        Intent intent = context.getPackageManager()
                                .getLaunchIntentForPackage( context.getPackageName() );

                        PendingIntent pending = PendingIntent.getActivity(
                                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        manager.set(AlarmManager.RTC, System.currentTimeMillis() + 300, pending);
                        System.exit(2);
                    } else {
                        // handle error
                    }
                }
            };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                return;
            }
            else if (purchase.getSku().equals(ITEM_SKU)) {
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    public static void catsShunYou(){
        ropeFade.setAnimationListener(new MyAnimationListener());
        imageView.startAnimation(ropeFade);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(context);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
//        consumeItem();
        if(mHelper != null){
            mHelper.dispose();
        }
        mHelper = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private static class MyAnimationListener implements Animation.AnimationListener{

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();

        @Override
        public void onAnimationStart(Animation animation) {
            layoutParams.width = Math.round(TwoRooms.screenWidthDp * TwoRooms.densityMultiple);
            layoutParams.height = Math.round(TwoRooms.screenWidthDp * 9 * TwoRooms.densityMultiple / 10);
            imageView.setLayoutParams(layoutParams);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            imageView.clearAnimation();
            imageView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mHelper == null) return;
        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}