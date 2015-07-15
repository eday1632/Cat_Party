package www.appawareinc.org.catparty;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;

import inappbilling.BuildKey;
import inappbilling.IabHelper;
import inappbilling.IabResult;
import inappbilling.Inventory;
import inappbilling.Purchase;

public class NoVIPAccess extends Fragment {
    private OnFragmentInteractionListener mListener;
    private static Context context;
    private static View rootView;
    boolean mIsPremium = false;
    static String SKU_PREMIUM = "vip_access2";
    static final int RC_REQUEST = 95035;
    public static IabHelper mHelper;

    public static NoVIPAccess newInstance(Context mContext) {
        NoVIPAccess fragment = new NoVIPAccess();
        context = mContext;

        return fragment;
    }

    public static void hideViews() {
        rootView.findViewById(R.id.bouncer).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.purchase).setVisibility(View.INVISIBLE);
    }

    public static void showViews() {
        rootView.findViewById(R.id.bouncer).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.purchase).setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.no_vip_access, container, false);

        runTaskInBackground("initializeVIP");

        Button purchase = (Button) rootView.findViewById(R.id.purchase);
        purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    onUpgradeAppButtonClicked();

                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        });

        BuildKey key = new BuildKey();
        String base64EncodedPublicKey = key.getKey();

        // Create the helper, passing it our context and the public key to verify signatures with
        mHelper = new IabHelper(context, base64EncodedPublicKey);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });

        return rootView;
    }

    private void runTaskInBackground(String task){
        Intent serviceIntent = new Intent(context, MultiIntentService.class);
        serviceIntent.putExtra("controller", task);
        context.startService(serviceIntent);
    }

    public void restartForVIPAccess(){
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage( context.getPackageName() );

        PendingIntent pending = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, System.currentTimeMillis() + 300, pending);
        System.exit(2);
    }

    public static void catsShunYou() {
        ImageView imageView = (ImageView) rootView.findViewById(R.id.bouncer);
        Button purchase = (Button) rootView.findViewById(R.id.purchase);

        Animation tigerBouncer = AnimationUtils.loadAnimation(context, R.anim.rope_fade);
        Animation buttonFadeIn = AnimationUtils.loadAnimation(context, R.anim.donate_button_fade_in);

        tigerBouncer.setAnimationListener(new MyAnimationListener(imageView));
        buttonFadeIn.setAnimationListener(new MyOtherAnimationListener(purchase));

        imageView.startAnimation(tigerBouncer);
        purchase.startAnimation(buttonFadeIn);
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private static class MyAnimationListener implements Animation.AnimationListener {
        ViewGroup.LayoutParams layoutParams;
        ImageView tiger;

        public MyAnimationListener(ImageView imageView) {
            tiger = imageView;
            layoutParams = tiger.getLayoutParams();
            layoutParams.width = Math.round(TwoRooms.screenWidthDp * TwoRooms.densityMultiple);
            layoutParams.height = Math.round(TwoRooms.screenWidthDp * 9 * TwoRooms.densityMultiple / 10);
            tiger.setLayoutParams(layoutParams);
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            tiger.clearAnimation();
            tiger.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private static class MyOtherAnimationListener implements Animation.AnimationListener {
        private Button purchase;

        public MyOtherAnimationListener(Button button) {
            purchase = button;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            purchase.clearAnimation();
            purchase.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                return;
            }
            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            if(mIsPremium) mHelper.consumeAsync(premiumPurchase, mConsumeFinishedListener);

        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
            }
        }
    };


    // User clicked the "Upgrade to Premium" button.
    public void onUpgradeAppButtonClicked() {

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        final String payload = "";

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.donation_amount_dialog);
        dialog.getWindow().setLayout(Math.round(280 * TwoRooms.densityMultiple),
                Math.round(320 * TwoRooms.densityMultiple)); //width, height

        RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio1) {
                    SKU_PREMIUM = "vip_access";
                } else if (checkedId == R.id.radio2) {
                    SKU_PREMIUM = "vip_access2";
                } else if (checkedId == R.id.radio5) {
                    SKU_PREMIUM = "vip_access5";
                } else if (checkedId == R.id.radio10) {
                    SKU_PREMIUM = "vip_access10";
                }
            }
        });

        Button yesDelete = (Button) dialog.findViewById(R.id.buttonOkay);
        yesDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHelper.launchPurchaseFlow(getActivity(), SKU_PREMIUM, RC_REQUEST,
                        mPurchaseFinishedListener, payload);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * Verifies the developer payload of a purchase.
     */
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

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                return;
            }

            if (purchase.getSku().equals(SKU_PREMIUM)) {
                // bought the premium upgrade!
                mIsPremium = true;

                SharedPreferences prefs = context.getSharedPreferences("vip_access", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("granted", 2);//1 for testing, 2 for production
                editor.apply();

                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.restart_dialog);
                dialog.getWindow().setLayout(Math.round(280 * TwoRooms.densityMultiple),
                        Math.round(244 * TwoRooms.densityMultiple)); //width, height

                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        restartForVIPAccess();
                    }
                });

                Button yesDelete = (Button) dialog.findViewById(R.id.buttonOkay);
                yesDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }
    };

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }
}

