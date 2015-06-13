package www.appawareinc.org.catparty;

import android.app.Activity;
import android.os.Bundle;

/* this class launches the "About Cat Party" screen when the menu button is selected and the
* "Cheetahs need us!" option is selected */
public class AboutCatParty extends Activity {

    /*lays out the view for the about screen*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }
}
